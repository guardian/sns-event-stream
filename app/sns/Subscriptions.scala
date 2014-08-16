package sns

import com.amazonaws.services.sns.AmazonSNSAsyncClient
import com.amazonaws.services.sns.model.SubscribeRequest

object Subscriptions {
  val HttpProtocol = "http"

  def registerEndpoint(client: AmazonSNSAsyncClient, url: String, topicArn: String) =
    client.subscribeAsync(
      new SubscribeRequest()
        .withEndpoint(url)
        .withProtocol(HttpProtocol)
        .withTopicArn(topicArn))

}
