package autoscaling

import play.api.libs.json.Json

object Notification {
  implicit val jsonReads = Json.reads[Notification]

  val TerminationEvent = "autoscaling:EC2_INSTANCE_TERMINATE"
}

case class Notification(
  StatusCode: String,
  Service: String,
  AutoScalingGroupName: String,
  Description: String,
  ActivityId: String,
  Event: String,
  Details: Map[String, String],
  AutoScalingGroupARN: String,
  Progress: Int,
  Time: String,
  AccountId: String,
  RequestId: String,
  StatusMessage: String,
  EndTime: String,
  EC2InstanceId: String,
  StartTime: String,
  Cause: String
)