import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.sns.AmazonSNSAsyncClient
import com.amazonaws.services.sns.model.SubscribeRequest
import dynamodb.StateTable
import grizzled.slf4j.Logging
import play.api.mvc.{Result, Handler, RequestHeader}
import play.api.{Application, GlobalSettings}

import ec2._
import sns._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Global extends GlobalSettings with Logging {
  override def onStart(app: Application): Unit = {
    super.onStart(app)

    val snsTopicArn = app.configuration.getString("sns.topic_arn").getOrElse({
      throw new RuntimeException("sns.topic_arn must be defined in config!")
    })

    val client = new AmazonSNSAsyncClient()
    client.setRegion(Region.getRegion(Regions.EU_WEST_1))

    InstanceMetadata.publicHostname() onComplete {
      case Failure(error) =>
        logger.error("Unable to look up hostname in instance metadata - are you on EC2?", error)

      case Success(hostname) =>
        logger.info(s"According to EC2 my hostname is $hostname")

        val url = s"http://$hostname:9000/broadcast"

        logger.info(s"Subscribing the SNS topic to POST to this endpoint: $url")

        client.subscribeFuture(
          new SubscribeRequest().withEndpoint(url).withProtocol("http").withTopicArn(snsTopicArn)
        ) onComplete {
          case Success(response) if Option(response.getSubscriptionArn).isDefined =>
            logger.info(s"Subscription arn: ${response.getSubscriptionArn}")

            InstanceMetadata.instanceId() onComplete {
              case Success(instanceId) =>
                logger.info(s"Successfully looked up instance ID: $instanceId")

                StateTable.recordSubscription(instanceId, url) onComplete {
                  case Success(_) =>
                    logger.info("Successfully recorded subscription in application state table")
                  case Failure(error) =>
                    logger.error("Unable to record subscription in application state table", error)
                }

              case Failure(error) =>
                logger.error("Unable to look up instance ID through metadata", error)
                logger.error("Could not record subscription in application state table")
            }

          case Success(_) => logger.info("Successfully sent subscription request to SNS topic")

          case Failure(error) => logger.error("Error subscribing to SNS", error)
        }
    }
  }

  override def onRequestReceived(request: RequestHeader): (RequestHeader, Handler) = {
    logger.info(s"Request received: ${request.method} ${request.uri}")
    super.onRequestReceived(request)
  }

  override def onBadRequest(request: RequestHeader, error: String): Future[Result] = {
    logger.info(s"Bad request, $error")

    super.onBadRequest(request, error)
  }
}
