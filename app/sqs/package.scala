import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.regions.Region
import com.amazonaws.services.sqs.AmazonSQSAsyncClient
import com.amazonaws.services.sqs.model._

import java.util.concurrent.{Future => JavaFuture}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

package object sqs {
  implicit class RichAmazonSQSAsyncClient(client: AmazonSQSAsyncClient) {
    private def createHandler[A <: com.amazonaws.AmazonWebServiceRequest, B]() = {
      val promise = Promise[B]()

      val handler = new AsyncHandler[A, B] {
        override def onSuccess(request: A, result: B): Unit = promise.complete(Success(result))

        override def onError(exception: Exception): Unit = promise.complete(Failure(exception))
      }

      (promise.future, handler)
    }

    private def asFuture[A <: com.amazonaws.AmazonWebServiceRequest, B](f: AsyncHandler[A, B] => JavaFuture[B]) = {
      val (future, handler) = createHandler[A, B]()
      f(handler)
      future
    }

    def receiveMessageFuture(request: ReceiveMessageRequest): Future[ReceiveMessageResult] =
      asFuture[ReceiveMessageRequest, ReceiveMessageResult](client.receiveMessageAsync(request, _))

    def deleteMessageFuture(request: DeleteMessageRequest): Future[Void] =
      asFuture[DeleteMessageRequest, Void](client.deleteMessageAsync(request, _))

    def sendMessageFuture(request: SendMessageRequest): Future[SendMessageResult] =
      asFuture[SendMessageRequest, SendMessageResult](client.sendMessageAsync(request, _))

    def withRegion(region: Region) = {
      client.setRegion(region)
      client
    }
  }
}
