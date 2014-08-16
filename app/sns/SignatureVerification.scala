package sns

import java.security.cert.{CertificateFactory, X509Certificate}

import com.amazonaws.services.sns.util.SignatureChecker
import com.amazonaws.util.StringInputStream
import play.api.libs.ws.WS

import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SignatureVerification {
  def getCertificate(message: SnsMessage): Future[X509Certificate] =
    WS.url(message.SigningCertURL).get map { response =>
      val inStream = new StringInputStream(response.body)
      val certificateFactory = CertificateFactory.getInstance("X.509")
      certificateFactory.generateCertificate(inStream).asInstanceOf[X509Certificate]
    }

  def verify(message: String, certificate: X509Certificate) = {
    val checker = new SignatureChecker
    checker.verifyMessageSignature(message, certificate.getPublicKey)
  }
}
