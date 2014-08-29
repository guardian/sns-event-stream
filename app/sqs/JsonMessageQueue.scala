package sqs

import com.amazonaws.services.sqs.AmazonSQSAsyncClient
import com.amazonaws.services.sqs.model._
import play.api.libs.json.{Writes, Json, Reads}
import com.gu.awswrappers._

import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global

case class MessageId(get: String) extends AnyVal
case class ReceiptHandle(get: String) extends AnyVal
case class Message[A](id: MessageId, get: A, handle: ReceiptHandle)

/** Utility class for SQS queues that use JSON to serialize their messages */
case class JsonMessageQueue[A](client: AmazonSQSAsyncClient, queueUrl: String)
                              (implicit executionContext: ExecutionContext) {
  import scala.collection.JavaConverters._

  def receive(request: ReceiveMessageRequest)(implicit reads: Reads[A]): Future[Seq[Message[A]]] =
    client.receiveMessageFuture(
      request.withQueueUrl(queueUrl)
    ) map { response =>
      response.getMessages.asScala.toSeq map { message =>
        Message(
          MessageId(message.getMessageId),
          Json.fromJson[A](Json.parse(message.getBody)) getOrElse {
            throw new RuntimeException(s"Couldn't parse JSON for message with ID ${message.getMessageId}")
          },
          ReceiptHandle(message.getReceiptHandle)
        )
      }
    }

  def receiveOne(request: ReceiveMessageRequest)(implicit reads: Reads[A]): Future[Option[Message[A]]] = {
    receive(request.withMaxNumberOfMessages(1)) map { messages =>
      messages.toList match {
        case message :: Nil => Some(message)
        case Nil => None
        case _ => throw new RuntimeException(s"Asked for 1 message from queue but got ${messages.length}")
      }
    }
  }

  def send(a: A)(implicit writes: Writes[A]): Future[SendMessageResult] = {
    client.sendMessageFuture(new SendMessageRequest()
      .withQueueUrl(queueUrl)
      .withMessageBody(Json.stringify(Json.toJson(a)))
    )
  }

  def delete(handle: ReceiptHandle): Future[Unit] = {
    client.deleteMessageFuture(new DeleteMessageRequest()
      .withQueueUrl(queueUrl)
      .withReceiptHandle(handle.get)
    ).map(_ => ())
  }
}