package models

import javax.inject.Singleton

import com.google.inject.{ImplementedBy, Inject}
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

case class CodeSnippet(label: String, code: String, analysisResult: Option[AnalysisResult] = None)

@ImplementedBy(classOf[InMemoryCodeSnipeptStore])
trait CodeSnippetStore {
  def all: Future[Seq[(Int, CodeSnippet)]]

  def byId(id: Int): Future[Option[CodeSnippet]]

  def save(snippet: CodeSnippet): Future[Int]
}

@Singleton
class InMemoryCodeSnipeptStore @Inject()(analysisEngine: AnalysisEngine) extends CodeSnippetStore {
  private var snippets = Seq[CodeSnippet]()

  override def all: Future[Seq[(Int, CodeSnippet)]] = Future {
    synchronized {
      snippets.zipWithIndex.map(_.swap)
    }
  }

  override def byId(id: Int): Future[Option[CodeSnippet]] = Future {
    synchronized {
      snippets.drop(id).headOption
    }
  }

  private def saveToBackend(snippet: CodeSnippet): Future[Int] = {
    Future {
      synchronized {
        val copied = snippet.copy()
        snippets :+= copied
        snippets.indexWhere(_ eq copied)
      }
    }
  }

  override def save(snippet: CodeSnippet): Future[Int] = {
    for {
      id <- saveToBackend(snippet)
      analysisResult <- analysisEngine.analyzeSnippet(snippet.code)
    } yield {
      synchronized {
        snippets = snippets.updated(id, snippet.copy(analysisResult = Some(analysisResult)))
      }
      id
    }
  }
}