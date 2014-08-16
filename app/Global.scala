import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.sns.AmazonSNSAsyncClient
import com.amazonaws.services.sns.model.SubscribeRequest
import grizzled.slf4j.Logging
import play.api.{Application, GlobalSettings}

import ec2._
import sns._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Global extends GlobalSettings with Logging {
  override def onStart(app: Application): Unit = {
    super.onStart(app)

    val snsTopicArn = app.configuration.getString("sns.topic_arn").getOrElse({
      throw new RuntimeException("sns.topic_arn must be defined in config!")
    })

    val client = new AmazonSNSAsyncClient()
    client.setRegion(Region.getRegion(Regions.EU_WEST_1))

    InstanceMetadata.localIpv4() onComplete {
      case Failure(error) =>
        logger.error("Unable to look up ip in instance metadata - are you on EC2?", error)

      case Success(ipAddress) =>
        logger.info(s"According to EC2 my IP address is $ipAddress")

        val url = s"http://$ipAddress/broadcast"

        logger.info(s"Subscribing the SNS topic to POST to this endpoint: $url")

        client.subscribeFuture(
          new SubscribeRequest().withEndpoint(url).withProtocol("http").withTopicArn(snsTopicArn)
        ) onComplete {
          case Success(response) if Option(response.getSubscriptionArn).isDefined =>
            logger.info(s"Subscription arn: ${response.getSubscriptionArn}")

          case Success(_) => logger.info("Successfully sent subscription request to SNS topic")

          case Failure(error) => logger.error("Error subscribing to SNS", error)
        }
    }
  }
}
