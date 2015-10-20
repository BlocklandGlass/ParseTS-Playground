package models

import java.nio.file.Path
import javax.inject.Inject

import com.google.inject.ImplementedBy
import models.AnalysisResult.{Complaint, SourcePos}
import models.ParseTSAnalysisEngine.ParseFailedException
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import resource._
import utils.TemporaryFile

import scala.concurrent.Future

case class AnalysisResult(warnings: Seq[Complaint])

object AnalysisResult {

  case class SourcePos(line: Int, column: Int)

  case class Complaint(msg: String, pos: SourcePos)

}

@ImplementedBy(classOf[ParseTSAnalysisEngine])
trait AnalysisEngine {
  def analyzeSnippet(code: String): Future[AnalysisResult]
}

class ParseTSAnalysisEngine @Inject()(config: Configuration) extends AnalysisEngine {

  private case class RawComplaint(msg: String)

  private case class WithSourcePos[T](pos: SourcePos, value: T)

  private case class ParsedAnalysisResult(complaints: Seq[WithSourcePos[RawComplaint]])

  private val parsetsBin = config.getString("parsets.bin").get

  private def rawAnalysisResult(path: Path): String = {
    import scala.sys.process._

    val args = Seq(parsetsBin, "lint", path.toAbsolutePath.toString)
    try {
      args.!!
    } catch {
      case e: RuntimeException => throw new ParseFailedException(e)
    }
  }

  private def parsedAnalysisResult(path: Path): ParsedAnalysisResult = {
    import play.api.libs.functional.syntax._
    import play.api.libs.json._
    implicit val complaintReads = Json.reads[RawComplaint]
    implicit val sourcePosReads = Json.reads[SourcePos]
    implicit def withSourcePosReads[T: Reads] =
      (
        (JsPath \ "pos").read[SourcePos] and
        (JsPath \ "value").read[T]
      )(WithSourcePos.apply[T] _)
    implicit val resultReads = Json.reads[ParsedAnalysisResult]

    val raw = rawAnalysisResult(path)
    Json.parse(raw).as[ParsedAnalysisResult]
  }

  override def analyzeSnippet(code: String): Future[AnalysisResult] = Future {
    managed(TemporaryFile.createWithContents(code, "parsets-demo-snippet-", ".cs")).acquireAndGet { tempFile =>
      val rawAnalysis = parsedAnalysisResult(tempFile.path)
      AnalysisResult(rawAnalysis.complaints.map(complaint => Complaint(complaint.value.msg, complaint.pos)))
    }
  }
}

object ParseTSAnalysisEngine {

  private class ParseFailedException(cause: Throwable) extends RuntimeException(cause)

}
