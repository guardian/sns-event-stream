package sns

import helpers.ResourcesHelper
import org.specs2.mutable.Specification
import scala.xml.XML

class ConfirmationResponseSpec extends Specification with ResourcesHelper {
  "getSubscriptionArn" should {
    "correctly parse the XML and retrieve the ARN" in {
      val body = XML.loadString(slurpOrDie("SubscriptionResponse.xml"))

      ConfirmationResponse.getSubscriptionArn(body) mustEqual
        SnsSubscriptionArn("arn:aws:sns:eu-west-1:201359054765:SnsEventStreamCODE-AutoscalingNotificationsSnsTopic-4DDRF7Z22WQY:261ee134-2699-4a3d-8922-954d733d3634")
    }
  }
}
