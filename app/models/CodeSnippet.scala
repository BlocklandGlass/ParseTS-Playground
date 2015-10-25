package models

import com.google.inject.{ImplementedBy, Inject}
import models.Driver.api._
import play.api.db.slick._
import play.api.libs.concurrent.Execution.Implicits._
import slick.lifted.ProvenShape

import scala.concurrent.Future

case class CodeSnippet(label: String, code: String, ip: Option[String])

@ImplementedBy(classOf[SlickCodeSnippetStore])
trait CodeSnippetStore {
  def all: Future[Seq[(Int, CodeSnippet)]]

  def byId(id: Int): Future[Option[CodeSnippet]]

  def save(snippet: CodeSnippet): Future[Int]
}

class CodeSnippets(tag: Tag) extends Table[(Int, CodeSnippet)](tag, "code_snippets") {
  def id = column[Int]("id", O.PrimaryKey)

  def label = column[String]("label")

  def code = column[String]("code")

  def ip = column[Option[String]]("ip")

  def snippet = (label, code, ip) <>(CodeSnippet.tupled, CodeSnippet.unapply)

  override def * : ProvenShape[(Int, CodeSnippet)] = (id, snippet)
}

object CodeSnippets extends TableQuery(new CodeSnippets(_))

class SlickCodeSnippetStore @Inject()(analysisStore: AnalysisStore,
                                      val dbConfigProvider: DatabaseConfigProvider
                                       ) extends CodeSnippetStore with HasDatabaseConfigProvider[Driver] {
  private val allQ = CodeSnippets.sortBy(_.id.desc)

  private def byIdQ(id: Rep[Int]) = allQ.filter(_.id === id).map(_.snippet).take(1)

  private def saveQ(snippet: CodeSnippet) = CodeSnippets.map(_.snippet).returning(CodeSnippets.map(_.id)) += snippet

  private val compiledAllQ = Compiled(allQ)
  private val compiledByIdQ = Compiled(byIdQ _)

  override def all: Future[Seq[(Int, CodeSnippet)]] = db.run(compiledAllQ.result)

  override def byId(id: Int): Future[Option[CodeSnippet]] = db.run(compiledByIdQ(id).result.headOption)

  override def save(snippet: CodeSnippet): Future[Int] = {
    for {
      id <- db.run(saveQ(snippet))
      _ <- analysisStore.analyzeSnippet(id, snippet)
    } yield id
  }
}