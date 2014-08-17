package dynamodb

import com.amazonaws.regions.{Regions, Region}
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.dynamodbv2.model._
import ec2.InstanceId

import play.api.Play.current
import sns.SnsSubscriptionArn

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object StateTable {
  val client = new AmazonDynamoDBAsyncClient()
  client.setRegion(Region.getRegion(Regions.EU_WEST_1))

  val InstanceIdKey = "EC2InstanceId"
  val ArnKey = "SubscriptionArn"

  lazy val tableName = current.configuration.getString("dynamo_db.table_name") getOrElse {
    throw new RuntimeException("dynamo_db.table_name must be set in config. Cannot maintain application state!")
  }

  private def keyQuery(instanceId: InstanceId) = Map(
    InstanceIdKey -> new AttributeValue().withS(instanceId.get)
  )

  def recordSubscription(instanceId: InstanceId, arn: SnsSubscriptionArn) = {
    client.putItemFuture(new PutItemRequest().withItem(Map(
      InstanceIdKey -> new AttributeValue().withS(instanceId.get),
      ArnKey -> new AttributeValue().withS(arn.get)
    )).withTableName(tableName))
  }

  def getSubscriptionArn(instanceId: InstanceId): Future[SnsSubscriptionArn] = {
    client.getItemFuture(new GetItemRequest().withTableName(tableName).withKey(keyQuery(instanceId))) map { result =>
      val attribute = result.getItem.asScala.getOrElse(ArnKey,
        throw new RuntimeException(s"No Arn defined for $instanceId")
      )

      Option(attribute.getS).map(SnsSubscriptionArn.apply).getOrElse(
        throw new RuntimeException(s"Arn defined for $instanceId but was not String, $attribute")
      )
    }
  }

  def deleteSubscription(instanceId: InstanceId) = {
    client.deleteItemFuture(new DeleteItemRequest().withKey(keyQuery(instanceId)))
  }
}
