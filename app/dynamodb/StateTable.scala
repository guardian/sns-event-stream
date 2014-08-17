package dynamodb

import com.amazonaws.regions.{Regions, Region}
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.dynamodbv2.model._
import ec2.InstanceId

import play.api.Play.current

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object StateTable {
  val client = new AmazonDynamoDBAsyncClient()
  client.setRegion(Region.getRegion(Regions.EU_WEST_1))

  val InstanceIdKey = "EC2InstanceId"
  val EndpointKey = "endpoint"

  lazy val tableName = current.configuration.getString("dynamo_db.table_name") getOrElse {
    throw new RuntimeException("dynamo_db.table_name must be set in config. Cannot maintain application state!")
  }

  private def keyQuery(instanceId: InstanceId) = Map(
    InstanceIdKey -> new AttributeValue().withS(instanceId.get)
  )

  def recordSubscription(instanceId: InstanceId, endpoint: String) = {
    client.putItemFuture(new PutItemRequest().withItem(Map(
      InstanceIdKey -> new AttributeValue().withS(instanceId.get),
      EndpointKey -> new AttributeValue().withS(endpoint)
    )).withTableName(tableName))
  }

  def getSubscriptionEndpoint(instanceId: InstanceId): Future[String] = {
    client.getItemFuture(new GetItemRequest().withTableName(tableName).withKey(keyQuery(instanceId))) map { result =>
      val attribute = result.getItem.asScala.getOrElse(EndpointKey,
        throw new RuntimeException(s"No endpoint defined for $instanceId")
      )

      Option(attribute.getS).getOrElse(
        throw new RuntimeException(s"Endpoint defined for $instanceId but was not String, $attribute")
      )
    }
  }

  def deleteSubscription(instanceId: InstanceId) = {
    client.deleteItemFuture(new DeleteItemRequest().withKey(keyQuery(instanceId)))
  }
}
