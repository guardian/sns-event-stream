package sns

import org.joda.time.DateTime
import play.api.libs.json._
import json.requireField

object SnsMessage {
  implicit val jsonReads = new Reads[SnsMessage] {
    override def reads(json: JsValue): JsResult[SnsMessage] = {
      SubscriptionConfirmationMessage.jsonReads.reads(json) orElse
        NotificationMessage.jsonReads.reads(json)
    }
  }
}

sealed trait SnsMessage {
  val SigningCertURL: String
}

object SubscriptionConfirmationMessage {
  implicit val jsonReads = requireField("Type", JsString("SubscriptionConfirmation"))
    .flatMap(_ => Json.reads[SubscriptionConfirmationMessage])
}

case class SubscriptionConfirmationMessage(
  MessageId: String,
  Token: String,
  TopicArn: String,
  Message: String,
  SubscribeURL: String,
  Timestamp: DateTime,
  SignatureVersion: String,
  Signature: String,
  SigningCertURL: String
) extends SnsMessage


object NotificationMessage {
  implicit val jsonReads = requireField("Type", JsString("Notification"))
    .flatMap(_ => Json.reads[NotificationMessage])
}

case class NotificationMessage(
  MessageId: String,
  TopicArn: String,
  Subject: String,
  Message: String,
  Timestamp: DateTime,
  SignatureVersion: String,
  Signature: String,
  SigningCertURL: String,
  UnsubscribeURL: String
) extends SnsMessage