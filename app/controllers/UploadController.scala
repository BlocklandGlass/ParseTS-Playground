package controllers

import javax.inject.Inject

import controllers.UploadController.UploadForm
import models.{CodeSnippet, CodeSnippetStore}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import utils.CAPTCHA

import scala.concurrent.Future

class UploadController @Inject() (val messagesApi: MessagesApi, snippetStore: CodeSnippetStore, captcha: CAPTCHA) extends Controller with I18nSupport {
  val uploadForm = Form(mapping(
    "label" -> nonEmptyText,
    "code" -> nonEmptyText
  )(UploadForm.apply)(UploadForm.unapply))

  def upload = Action { implicit req =>
    Ok(views.html.upload(uploadForm, captcha.display))
  }

  def doUpload = Action.async { implicit req =>
    (for {
      form <- Future.successful(uploadForm.bindFromRequest())
      form <- captcha.validatingForm(form)
    } yield form.fold(
      form => Future.successful(BadRequest(views.html.upload(form, captcha.display))),
      upload => saveUpload(upload).map(id => Redirect(routes.ListingController.byId(id)))
    )).flatMap(identity)
  }

  private def saveUpload(upload: UploadForm)(implicit req: RequestHeader): Future[Int] = {
    val snippet = CodeSnippet(upload.label, upload.code, Some(req.remoteAddress))
    snippetStore.save(snippet)
  }
}

object UploadController {
  case class UploadForm(label: String, code: String)
}
