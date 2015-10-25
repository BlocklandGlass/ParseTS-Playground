package utils

import java.security.MessageDigest
import java.util
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

import com.google.common.io.BaseEncoding
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSAPI
import play.api.mvc.{AnyContent, Request}
import play.twirl.api.Html
import utils.ReCAPTCHA.{SecureToken, ReCAPTCHAResponse}

import scala.concurrent.Future

class ReCAPTCHA @Inject()(config: ReCAPTCHAConfig, ws: WSAPI) extends CAPTCHA {
  private val secureTokenKey = {
    val key = config.secretKey.getBytes("UTF-8")
    new SecretKeySpec(util.Arrays.copyOf(MessageDigest.getInstance("SHA").digest(key), 16), "AES")
  }

  private def secureToken = {
    implicit val writes = Json.writes[SecureToken]
    val sessionId = UUID.randomUUID().toString
    val time = System.currentTimeMillis()
    val token = SecureToken(sessionId, time)
    val json = Json.toJson(token).toString()
    encryptSecureToken(json)
  }

  private def encryptSecureToken(token: String) = {
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, secureTokenKey)
    BaseEncoding.base64Url().omitPadding().encode(cipher.doFinal(token.getBytes("UTF-8")))
  }

  override def display: Html = views.html.recaptcha(config.siteKey, secureToken)

  override def verify(implicit request: Request[AnyContent]): Future[Boolean] = {
    implicit val reads = Json.reads[ReCAPTCHAResponse]
    val response = request.body.asFormUrlEncoded.get.getOrElse("g-recaptcha-response", return Future.successful(false))
    ws.url("https://www.google.com/recaptcha/api/siteverify").post(Map(
      "secret" -> Seq(config.secretKey),
      "response" -> Seq(response.head),
      "remoteip" -> Seq(request.remoteAddress)
    )).map(_.json.as[ReCAPTCHAResponse].success)
  }
}

object ReCAPTCHA {

  case class ReCAPTCHAResponse(success: Boolean)

  case class SecureToken(session_id: String, ts_ms: Long)

}

case class ReCAPTCHAConfig(siteKey: String, secretKey: String)

object ReCAPTCHAConfig {
  def fromConfig(config: Configuration): ReCAPTCHAConfig = {
    ReCAPTCHAConfig(
      config.getString("siteKey").getOrElse(throw new RuntimeException("No ReCAPTCHA siteKey configured")),
      config.getString("secretKey").getOrElse(throw new RuntimeException("No ReCAPTCHA secretKey configured"))
    )
  }
}