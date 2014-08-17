package autoscaling

import play.api.libs.json.Json

object Notification {
  implicit val jsonReads = Json.reads[Notification]

  val TerminationEvent = "autoscaling:EC2_INSTANCE_TERMINATE"
}

case class Notification(
  StatusCode: Option[String],
  Service: String,
  AutoScalingGroupName: String,
  Description: Option[String],
  ActivityId: Option[String],
  Event: String,
  Details: Option[Map[String, String]],
  AutoScalingGroupARN: String,
  Progress: Option[Int],
  Time: String,
  AccountId: String,
  RequestId: String,
  StatusMessage: Option[String],
  EndTime: Option[String],
  EC2InstanceId: Option[String],
  StartTime: Option[String],
  Cause: Option[String]
)