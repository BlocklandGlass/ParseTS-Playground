package controllers

import javax.inject.Inject

import models.{AnalysisStore, CodeSnippetStore}
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits._

class ListingController @Inject() (snippetStore: CodeSnippetStore, analysisStore: AnalysisStore) extends Controller {
  def all = Action.async {
    snippetStore.all.map(snippets => Ok(views.html.list(snippets)))
  }

  def byId(id: Int) = Action.async {
    for {
      snippet <- snippetStore.byId(id)
      analysis <- analysisStore.bySnippetId(id)
    } yield snippet.fold(NotFound(views.html.notFound()))(snippet => Ok(views.html.snippet(snippet, analysis)))
  }
}
