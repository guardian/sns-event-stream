package autoscaling

import com.amazonaws.regions.{Regions, Region}
import com.amazonaws.services.sns.model.UnsubscribeRequest
import com.amazonaws.services.sqs.AmazonSQSAsyncClient
import dynamodb.StateTable
import ec2.InstanceId
import sqs.{JsonMessageQueue, Message, JsonQueueWorker, RichAmazonSQSAsyncClient}
import sns.{client => snsClient, RichAmazonSNSAsyncClient}

import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object AutoscalingNotificationsQueueWorker extends JsonQueueWorker[Notification] {
  override val queue: JsonMessageQueue[Notification] =
    JsonMessageQueue[Notification](
      new AmazonSQSAsyncClient().withRegion(Region.getRegion(Regions.EU_WEST_1)),
      current.configuration.getString("autoscaling_notifications.queue_arn") getOrElse {
        throw new RuntimeException("Required config property autoscaling_notifications.queue_arn not in config!")
      }
    )

  override def process(message: Message[Notification]): Future[Unit] = {
    val notification = message.get

    if (notification.Event == Notification.TerminationEvent) {
      val instanceId = InstanceId(notification.EC2InstanceId)

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
      Future.successful(())
    }
  }
}
