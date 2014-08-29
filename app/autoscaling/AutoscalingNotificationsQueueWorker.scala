package autoscaling

import com.amazonaws.regions.{Regions, Region}
import com.amazonaws.services.sns.model.UnsubscribeRequest
import com.amazonaws.services.sqs.AmazonSQSAsyncClient
import dynamodb.StateTable
import ec2.InstanceId
import play.api.libs.json.Json
import sqs.{JsonMessageQueue, Message, JsonQueueWorker}
import sns.{client => snsClient}
import com.gu.awswrappers._

import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object AutoscalingNotificationsQueueWorker extends JsonQueueWorker[sns.NotificationMessage] {
  override val queue: JsonMessageQueue[sns.NotificationMessage] =
    JsonMessageQueue[sns.NotificationMessage](
      {
        val client = new AmazonSQSAsyncClient()
        client.setRegion(Region.getRegion(Regions.EU_WEST_1))
        client
      },
      current.configuration.getString("autoscaling_notifications.queue_url") getOrElse {
        throw new RuntimeException("Required config property autoscaling_notifications.queue_arn not in config!")
      }
    )

  override def process(message: Message[sns.NotificationMessage]): Future[Unit] = {
    val notification = Json.fromJson[Notification](Json.parse(message.get.Message)) getOrElse {
      throw new RuntimeException("Could not parse message body")
    }

    if (notification.Event == Notification.TerminationEvent) {
      val instanceId = InstanceId(notification.EC2InstanceId.get)

      val ftr = for {
        arn <- StateTable.getSubscriptionArn(instanceId)
        _ <- snsClient.unsubscribeFuture(new UnsubscribeRequest().withSubscriptionArn(arn.get))
        _ <- StateTable.deleteSubscription(instanceId)
      } yield ()

      logger.info(s"Removing subscription for expired instance $instanceId")

      ftr onComplete {
        case Success(_) => logger.info(s"Successfully removed subscription for $instanceId")
        case Failure(error) =>
          logger.error(s"Failed to remove subscription for $instanceId", error)
      }

      ftr
    } else {
      logger.info(s"Ignoring ${notification.Event} event (${notification.RequestId})")
      Future.successful(())
    }
  }
}
