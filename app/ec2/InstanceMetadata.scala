package ec2

import play.api.libs.ws.WS
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global

case class InstanceId(get: String) extends AnyVal

object InstanceMetadata {
  /** Private (to AWS) IP of the EC2 instance */
  def localIpv4() =
    WS.client.url("http://169.254.169.254/latest/meta-data/local-ipv4").get().map(_.body)

  def publicHostname() =
    WS.client.url("http://169.254.169.254/latest/meta-data/public-hostname").get().map(_.body)

  def instanceId() =
    WS.client.url("http://169.254.169.254/latest/meta-data/instance-id").get()
      .map(response => InstanceId(response.body))
}
