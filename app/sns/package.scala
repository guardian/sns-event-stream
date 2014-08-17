import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.regions.{Regions, Region}
import com.amazonaws.services.sns.AmazonSNSAsyncClient

import java.util.concurrent.{Future => JavaFuture}
import com.amazonaws.services.sns.model.{UnsubscribeRequest, SubscribeResult, SubscribeRequest}

import scala.concurrent.{Future, Promise}
import scala.util.{Success, Failure}

package object sns {
  val client = new AmazonSNSAsyncClient()
  client.setRegion(Region.getRegion(Regions.EU_WEST_1))

  case class SnsSubscriptionArn(get: String) extends AnyVal

  implicit class RichAmazonSNSAsyncClient(client: AmazonSNSAsyncClient) {
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

    def subscribeFuture(subscribeRequest: SubscribeRequest): Future[SubscribeResult] =
      asFuture[SubscribeRequest, SubscribeResult](client.subscribeAsync(subscribeRequest, _))

    def unsubscribeFuture(unsubscribeRequest: UnsubscribeRequest): Future[Void] =
      asFuture[UnsubscribeRequest, Void](client.unsubscribeAsync(unsubscribeRequest, _))
  }
}
