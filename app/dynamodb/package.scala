import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.amazonaws.services.dynamodbv2.model._

import scala.concurrent.Promise
import scala.util.{Failure, Success}

package object dynamodb {
  implicit class RichDynamoDBAsyncClient(client: AmazonDynamoDBAsyncClient) {
    private def createHandler[A <: com.amazonaws.AmazonWebServiceRequest, B]() = {
      val promise = Promise[B]()

      val handler = new AsyncHandler[A, B] {
        override def onSuccess(request: A, result: B): Unit = promise.complete(Success(result))

        override def onError(exception: Exception): Unit = promise.complete(Failure(exception))
      }

      (promise.future, handler)
    }

    private def asFuture[A <: com.amazonaws.AmazonWebServiceRequest, B](f: AsyncHandler[A, B] => Any) = {
      val (future, handler) = createHandler[A, B]()
      f(handler)
      future
    }

    def getItemFuture(request: GetItemRequest) =
      asFuture[GetItemRequest, GetItemResult](client.getItemAsync(request, _))

    def batchGetItemFuture(request: BatchGetItemRequest) =
      asFuture[BatchGetItemRequest, BatchGetItemResult](client.batchGetItemAsync(request, _))

    def putItemFuture(request: PutItemRequest) =
      asFuture[PutItemRequest, PutItemResult](client.putItemAsync(request, _))

    def batchWriteItemFuture(request: BatchWriteItemRequest) = {
      asFuture[BatchWriteItemRequest, BatchWriteItemResult](client.batchWriteItemAsync(request, _))
    }

    def scanFuture(request: ScanRequest) =
      asFuture[ScanRequest, ScanResult](client.scanAsync(request, _))

    def deleteItemFuture(request: DeleteItemRequest) =
      asFuture[DeleteItemRequest, DeleteItemResult](client.deleteItemAsync(request, _))

    def queryFuture(request: QueryRequest) =
      asFuture[QueryRequest, QueryResult](client.queryAsync(request, _))

    def updateItemFuture(request: UpdateItemRequest) =
      asFuture[UpdateItemRequest, UpdateItemResult](client.updateItemAsync(request, _))
  }
}
