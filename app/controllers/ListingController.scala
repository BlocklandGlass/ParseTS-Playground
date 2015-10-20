package controllers

import javax.inject.Inject

import models.CodeSnippetStore
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits._

class ListingController @Inject() (snippetStore: CodeSnippetStore) extends Controller {
  def all = Action.async {
    snippetStore.all.map(snippets => Ok(views.html.list(snippets)))
  }

  def byId(id: Int) = Action.async {
    snippetStore.byId(id).map(snippet => Ok.apply(views.html.snippet(snippet.get)))
  }
}
