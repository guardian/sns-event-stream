package controllers

import play.api._
import play.api.libs.EventSource
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import play.api.libs.concurrent.Promise
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {
  val heartBeat = Enumerator.repeatM(Promise.timeout((), 1 second)).map(_ => "thump")

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def events = Action { implicit request =>
    Ok.feed(heartBeat &> EventSource()).as("text/event-stream")
  }
}