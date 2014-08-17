package autoscaling

import helpers.ResourcesHelper
import org.specs2.mutable.Specification
import play.api.libs.json.Json

class NotificationSpec extends Specification with ResourcesHelper {
  "Notification" should {
    "deserialize a termination notification" in {
      val notification = slurpOrDie("TerminationNotification.json")

      val parsed = Json.fromJson[Notification](Json.parse(notification))

      parsed.asOpt must beSome.which(_.Event == Notification.TerminationEvent)
    }

    "deserialize a test notification" in {
      val notification = slurpOrDie("TestAutoscalingNotification.json")

      val parsed = Json.fromJson[Notification](Json.parse(notification))

      parsed.asOpt must beSome
    }
  }
}
