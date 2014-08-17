package sns

import scala.xml.NodeSeq

object ConfirmationResponse {
  def getSubscriptionArn(root: NodeSeq) =
    SnsSubscriptionArn((root \ "ConfirmSubscriptionResult" \ "SubscriptionArn").text.trim)
}
