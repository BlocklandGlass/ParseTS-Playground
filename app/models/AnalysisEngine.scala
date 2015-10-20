package models

import java.nio.file.Path
import javax.inject.Inject

import com.google.inject.ImplementedBy
import models.AnalysisResult.{Complaint, LinkedComplaint, SourcePos}
import models.Driver.api._
import models.ParseTSAnalysisEngine.ParseFailedException
import play.api.Configuration
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import play.api.libs.concurrent.Execution.Implicits._
import resource._
import slick.lifted.ProvenShape
import utils.TemporaryFile

import scala.concurrent.Future

case class AnalysisResult(complaints: Seq[Complaint])

object AnalysisResult {

  case class SourcePos(line: Int, column: Int)

  case class Complaint(msg: String, pos: SourcePos)

  case class LinkedComplaint(complaint: Complaint, analysisId: Int)

  case class Info(snippetId: Int)

}

class AnalysisComplaints(tag: Tag) extends Table[(Int, LinkedComplaint)](tag, "code_snippet_analysis_complaints") {
  def id = column[Int]("id", O.PrimaryKey)

  def analysisId = column[Int]("analysis_id")

  def line = column[Int]("line")

  def col = column[Int]("col")

  def message = column[String]("message")

  def analysis = foreignKey("analysis_fk", analysisId, AnalysisResults)(_.id)

  def pos = (line, col) <>(SourcePos.tupled, SourcePos.unapply)

  def complaint = (message, pos) <>(Complaint.tupled, Complaint.unapply)

  def linkedComplaint = (complaint, analysisId) <>(LinkedComplaint.tupled, LinkedComplaint.unapply)

  override def * : ProvenShape[(Int, LinkedComplaint)] = (id, linkedComplaint)
}

object AnalysisComplaints extends TableQuery(new AnalysisComplaints(_))

class AnalysisResults(tag: Tag) extends Table[(Int, AnalysisResult.Info)](tag, "code_snippet_analysis_results") {
  def id = column[Int]("id", O.PrimaryKey)

  def snippetId = column[Int]("snippet_id")

  def snippet = foreignKey("snippet_fk", snippetId, CodeSnippets)(_.id)

  def info = snippetId <>(AnalysisResult.Info.apply, AnalysisResult.Info.unapply)

  override def * : ProvenShape[(Int, AnalysisResult.Info)] = (id, info)
}

object AnalysisResults extends TableQuery(new AnalysisResults(_))

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

@ImplementedBy(classOf[SlickAnalysisStore])
trait AnalysisStore {
  def analyzeSnippet(snippetId: Int, snippet: CodeSnippet): Future[Unit]
  def bySnippetId(snippetId: Int): Future[Option[AnalysisResult]]
}

class SlickAnalysisStore @Inject()(analysisEngine: AnalysisEngine,
                                   val dbConfigProvider: DatabaseConfigProvider
                                    ) extends AnalysisStore with HasDatabaseConfigProvider[Driver] {
  private def complaintsByAnalysisIdQ(analysisId: Rep[Option[Int]]) = AnalysisComplaints.filter(_.analysisId === analysisId).map(_.complaint)
  private def analysisBySnippetIdQ(snippetId: Rep[Int]) = AnalysisResults.filter(_.snippetId === snippetId).map(_.id).take(1)

  private val compiledComplaintsByAnalysisIdQ = Compiled(complaintsByAnalysisIdQ _)
  private val compiledAnalysisBySnippetIdQ = Compiled(analysisBySnippetIdQ _)

  private def saveAnalysisQ(snippetId: Int) = AnalysisResults.map(_.snippetId).returning(AnalysisResults.map(_.id)) += snippetId
  private def saveComplaintQ(complaint: Complaint, analysisId: Int) = AnalysisComplaints.map(_.linkedComplaint) += LinkedComplaint(complaint, analysisId)
  private def saveComplaintsQ(complaints: Seq[Complaint], analysisId: Int) = DBIO.seq(complaints.map(saveComplaintQ(_, analysisId)): _*)

  override def analyzeSnippet(snippetId: Int, snippet: CodeSnippet): Future[Unit] = analysisEngine.analyzeSnippet(snippet.code) flatMap { analysis =>
    db.run((for {
      analysisId <- saveAnalysisQ(snippetId)
      _ <- saveComplaintsQ(analysis.complaints, analysisId)
    } yield ()).transactionally)
  }

  override def bySnippetId(snippetId: Int): Future[Option[AnalysisResult]] = db.run(for {
    analysisId <- compiledAnalysisBySnippetIdQ(snippetId).result.headOption
    complaints <- compiledComplaintsByAnalysisIdQ(analysisId).result
  } yield analysisId.map(_ => AnalysisResult(complaints)))
}
