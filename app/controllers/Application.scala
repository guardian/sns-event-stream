package controllers

import grizzled.slf4j.Logging
import sns.{NotificationMessage, SubscriptionConfirmationMessage, SnsMessage}

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
    Ok(views.html.index("Your new application is ready."))
  }

  /** TODO should be dependent on having successfully subscribed to the SNS topic */
  def healthcheck = Action {
    Ok
  }

  /** Endpoint that SNS broadcasts to
    *
    * TODO: Signature confirmation? Or use a separate embedded webserver on a different port that you don't expose
    * publicly (good practice to do that anyway).
    */
  def broadcast = Action(parse.json[SnsMessage]) { implicit request =>
    request.body match {
      case message: SubscriptionConfirmationMessage =>
        logger.info("Received subscription confirmation message:")
        logger.info(message)

        WS.client.url(message.SubscribeURL).get onComplete {
          case Success(response) if response.status == 200 =>
            logger.info("Successfully subscribed:")
            logger.info(response.body)

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
  }

  def events = Action { implicit request =>
    Ok.feed(messagesEnumerator &> EventSource()).as("text/event-stream")
  }
}