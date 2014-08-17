package sns

import helpers.ResourcesHelper
import org.specs2.mutable.Specification
import play.api.libs.json.{JsSuccess, Json}

class SnsMessageSpec extends Specification with ResourcesHelper {
  "SnsMessage" should {
    "correctly deserialize a subscription confirmation message" in {
      val confirmationMessage = slurpOrDie("SubscriptionConfirmation.json")

      Json.fromJson[SnsMessage](Json.parse(confirmationMessage)) mustEqual JsSuccess(SubscriptionConfirmationMessage(
        "b5d5cd7d-61af-4be9-b45f-2608f7f83ef1",
        "2336412f37fb687f5d51e6e241d638b056f59becf343413fff6cda0a9c5ad868b8112753e5e99eff9e50c31754b00dc404116195b8e67b10194855baba8762854cb8f41a7956e334b1575b144b2730d605a08916bf5f6a0b17668212b07b1dd9fc9baff2d68d9b5cd275cf1310c3fe90df441494e612f76e6f84b5cfb0d70f3f06525396911ab9641ea9c53df829d9ac",
        "arn:aws:sns:eu-west-1:201359054765:SnsEventStreamCODE-SnsTopic-A04OC2PHQTF1",
        "You have chosen to subscribe to the topic arn:aws:sns:eu-west-1:201359054765:SnsEventStreamCODE-SnsTopic-A04OC2PHQTF1.\nTo confirm the subscription, visit the SubscribeURL included in this message.",
        "https://sns.eu-west-1.amazonaws.com/?Action=ConfirmSubscription&TopicArn=arn:aws:sns:eu-west-1:201359054765:SnsEventStreamCODE-SnsTopic-A04OC2PHQTF1&Token=2336412f37fb687f5d51e6e241d638b056f59becf343413fff6cda0a9c5ad868b8112753e5e99eff9e50c31754b00dc404116195b8e67b10194855baba8762854cb8f41a7956e334b1575b144b2730d605a08916bf5f6a0b17668212b07b1dd9fc9baff2d68d9b5cd275cf1310c3fe90df441494e612f76e6f84b5cfb0d70f3f06525396911ab9641ea9c53df829d9ac",
        "2014-08-17T07:54:58.546Z",
        "1",
        "G40S1CEc1dALOKz/4paxZPPGs0t7X5DlSRRtcn1SCUwnB/99Fcyy3HC+gTPDG3+9g1Um0PGF0qhXO3BfLiXH7ZZHi6lOtQuvg1e+MIoVUknISn7AsJdSjqYlkLCWcvrdmIgM5o/RSu1aEngzdMP2wq5O0YuGY6W13O/K10Fu7+bDl39LmXHdEZRx30ZXIQmlMpKeoG2Fnnf2BVmPAFEiNsfeSN7cPYp69vQV0jluyjxMQ2ro2hSIovhi+5/rC0aY6HI9fTWWQd88hgHIfoDmBslc31PNFZEPmikhgo2IMC6be6zr8Qh/xH79Vd21pgs0V9tteIaeVBwoQBGEMdHeGg==",
        "https://sns.eu-west-1.amazonaws.com/SimpleNotificationService-e372f8ca30337fdb084e8ac449342c77.pem"
      ))
    }
  }
}
