package autoscaling

import helpers.ResourcesHelper
import org.specs2.mutable.Specification
import play.api.libs.json.Json

class NotificationSpec extends Specification with ResourcesHelper {
  "Notification" should {
    "deserialize" in {
      val notification = slurpOrDie("TerminationNotification.json")

      val parsed = Json.fromJson[Notification](Json.parse(notification))

      parsed.asOpt must beSome.which(_.Event == Notification.TerminationEvent)
    }
  }
}
