import com.amazonaws.regions.{Regions, Region}
import com.amazonaws.services.sns.AmazonSNSAsyncClient

package object sns {
  val client = new AmazonSNSAsyncClient()
  client.setRegion(Region.getRegion(Regions.EU_WEST_1))

  case class SnsSubscriptionArn(get: String) extends AnyVal
}
