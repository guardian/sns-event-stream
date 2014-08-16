package controllers

import grizzled.slf4j.Logging
import play.api._
import play.api.libs.EventSource
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import play.api.libs.concurrent.Promise
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller with Logging {
  val heartBeat = Enumerator.repeatM(Promise.timeout((), 1 second)).map(_ => "thump")

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  /** Endpoint that SNS broadcasts to.
    *
    * TODO switch this out to use an embedded web server that can run on a separate port.
    */
  def broadcast = Action { implicit request =>
    logger.info(request.headers.toMap)
    logger.info(request.body)
    NotFound
  }

  def events = Action { implicit request =>
    Ok.feed(heartBeat &> EventSource()).as("text/event-stream")
  }
}