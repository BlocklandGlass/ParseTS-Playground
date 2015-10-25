package utils

import play.api.data.Form
import play.api.inject.{Binding, Module}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{AnyContent, Request}
import play.api.{Configuration, Environment}
import play.twirl.api.Html

import scala.concurrent.Future

trait CAPTCHA {
  def display: Html

  def verify(implicit request: Request[AnyContent]): Future[Boolean]

  def validatingForm[T](form: Form[T])(implicit request: Request[AnyContent]): Future[Form[T]] = {
    verify.map { successful =>
      if (successful) {
        form
      } else {
        form.withError("captcha", "CAPTCHA verification failed")
      }
    }
  }
}

class CAPTCHAProvider extends Module {
  private def recaptchaConfig(configuration: Configuration): ReCAPTCHAConfig = {
    ReCAPTCHAConfig.fromConfig(configuration.getConfig("captcha.recaptcha").getOrElse(Configuration.empty))
  }

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[ReCAPTCHAConfig].to(recaptchaConfig(configuration))
    ) :+ (configuration.getString("captcha.provider") match {
      case Some("recaptcha") =>
        bind[CAPTCHA].to[ReCAPTCHA]
      case None =>
        bind[CAPTCHA].to(NoCAPTCHA)
      case provider => throw new RuntimeException(s"Unknown CAPTCHA provider: $provider")
    })
  }
}
