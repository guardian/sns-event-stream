package controllers

import dynamodb.StateTable
import ec2.InstanceMetadata
import grizzled.slf4j.Logging
import play.api.libs.json.Json
import sns._

import play.api._
import play.api.libs.EventSource
import play.api.libs.iteratee.Concurrent
import play.api.libs.ws.WS
import play.api.mvc._
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Application extends Controller with Logging {
  val (messagesEnumerator, messagesChannel) = Concurrent.broadcast[String]

  def index = Action {
    Ok(views.html.index())
  }

  /** TODO should be dependent on having successfully subscribed to the SNS topic */
  def healthcheck = Action {
    Ok
  }

  private def recordArn(arn: SnsSubscriptionArn): Unit = {
    InstanceMetadata.instanceId() onComplete {
      case Success(instanceId) =>
        logger.info(s"Successfully looked up instance ID: $instanceId")

        StateTable.recordSubscription(instanceId, arn) onComplete {
          case Success(_) =>
            logger.info("Successfully recorded subscription in application state table")
          case Failure(error) =>
            logger.error("Unable to record subscription in application state table", error)
        }

      case Failure(error) =>
        logger.error("Unable to look up instance ID through metadata", error)
        logger.error("Could not record subscription in application state table")
    }
  }

  /** Endpoint that SNS broadcasts to
    *
    * TODO: Must do signature verification.
    *
    * It might also be preferable to use a separate embedded webserver on another port for this, then restrict access
    * to that port to IPs within the ranges specified here -- https://forums.aws.amazon.com/ann.jspa?annID=2347 -- so
    * that we're certain the only requests that come in are from SNS.
    */
  def broadcast = Action { implicit request =>
    request.body.asText.flatMap(json => Json.fromJson[SnsMessage](Json.parse(json)).asOpt) match {
      case Some(message) => message match {
        case message: SubscriptionConfirmationMessage =>
          logger.info("Received subscription confirmation message:")
          logger.info(message)

          WS.client.url(message.SubscribeURL).get onComplete {
            case Success(response) if response.status == 200 =>
              logger.info("Successfully subscribed:")
              val arn = ConfirmationResponse.getSubscriptionArn(scala.xml.XML.loadString(response.body))
              logger.info(s"Subscription ARN: $arn")
              recordArn(arn)

            case Success(response) =>
              logger.error(s"Error subscribing to ${message.SubscribeURL}: ${response.status} ${response.statusText}")
              logger.error(response.body)

            case Failure(error) =>
              logger.error(s"Failed to subscribe to ${message.SubscribeURL}", error)
          }

          Ok

        case notification: NotificationMessage =>
          logger.info("Received notification message:")
          logger.info(notification)
          messagesChannel.push(notification.Message)
          Ok
      }

      case None =>
        logger.error(s"Body did not contain JSON ${request.body.toString}")
        BadRequest
    }
  }

  def events = Action { implicit request =>
    Ok.feed(messagesEnumerator &> EventSource()).as("text/event-stream")
  }
}