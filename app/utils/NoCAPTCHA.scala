package utils

import play.api.mvc.{AnyContent, Request}
import play.twirl.api.Html

import scala.concurrent.Future

object NoCAPTCHA extends CAPTCHA {
  override def display: Html = Html("")

  override def verify(implicit request: Request[AnyContent]): Future[Boolean] = Future.successful(true)
}
