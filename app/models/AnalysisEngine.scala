package models

import java.nio.file.{Path, Files}
import javax.inject.Inject

import com.google.inject.ImplementedBy
import models.AnalysisResult.{SourcePos, Complaint, WithSourcePos}
import models.ParseTSAnalysisEngine.ParseFailedException
import play.api.Configuration
import resource._
import utils.TemporaryFile

import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

case class AnalysisResult(warnings: Seq[WithSourcePos[Complaint]])

object AnalysisResult {
  case class Complaint(msg: String)
  case class SourcePos(line: Int, column: Int)
  case class WithSourcePos[T](pos: SourcePos, value: T)
}

@ImplementedBy(classOf[ParseTSAnalysisEngine])
trait AnalysisEngine {
  def analyzeSnippet(code: String): Future[AnalysisResult]
}

class ParseTSAnalysisEngine @Inject() (config: Configuration) extends AnalysisEngine {
  private case class ParsedAnalysisResult(complaints: Seq[WithSourcePos[Complaint]])
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
    import play.api.libs.json._
    import play.api.libs.functional.syntax._
    implicit val complaintReads = Json.reads[Complaint]
    implicit val sourcePosReads = Json.reads[SourcePos]
    implicit def withSourcePosReads[T: Reads] = (
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
      AnalysisResult(rawAnalysis.complaints)
    }
  }
}

object ParseTSAnalysisEngine {
  private class ParseFailedException(cause: Throwable) extends RuntimeException(cause)
}
