package sqs

import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import grizzled.slf4j.Logging
import play.api.libs.json.Reads

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object JsonQueueWorker {
  /** The maximum allowed long poll time on SQS:
    * http://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-long-polling.html
    */
  val WaitTimeSeconds = 20
}

/** Repeatedly long polls queue, using process method to process the job.
  *
  * Provides two
  *
  * @tparam A The job
  */
abstract class JsonQueueWorker[A: Reads] extends Logging {
  import JsonQueueWorker._

  val queue: JsonMessageQueue[A]

  def process(message: Message[A]): Future[Unit]

  final protected def getAndProcess: Future[Unit] = {
    val getRequest = queue.receiveOne(new ReceiveMessageRequest().withWaitTimeSeconds(WaitTimeSeconds))

    getRequest onComplete {
      case Success(Some(message @ Message(id, _, receipt))) =>
        process(message) onComplete {
          case Success(_) =>
            /** Ultimately, we ought to be able to recover from processing the same message twice anyway, as the nature
              * of SQS means you could get the same message delivered twice.
              */
            queue.delete(receipt) onFailure {
              case error => logger.error(s"Error deleting message $id from queue", error)
            }

          case Failure(error) =>
            logger.error(s"Error processing message $id", error)
        }

      case Success(None) =>
        logger.info(s"No message after $WaitTimeSeconds seconds")

      case Failure(error) =>
        logger.error("Encountered error receiving message from queue", error)
    }

    getRequest.map(_ => ())
  }

  final private def next() {
    getAndProcess onComplete {
      case _ => next()
    }
  }

  final private var started = false

  final def start() {
    synchronized {
      if (started) {
        logger.warn("Attempted to start queue worker but queue worker is already started")
      } else {
        logger.info("Starting worker ... ")
        started = true
        next()
      }
    }
  }
}