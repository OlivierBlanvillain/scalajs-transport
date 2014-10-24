package transport.autowire

import upickle._
import scala.concurrent._
import scala.collection.mutable
import transport._
import _root_.autowire.Core.Request
import scala.concurrent._

/** Pool of pending promises identified by integers. Promises are removed upon completion. */
class PendingPromises[T](implicit ec: ExecutionContext) {
  private var id = 0
  private val map = mutable.Map.empty[Int, Promise[T]]
  
  def next(): (Int, Future[T]) = {
    this.id = this.id + 1
    val newId = id
    val promise = Promise[T]()
    promise.future.onComplete { case _ => map.remove(newId) }
    map.update(newId, promise)
    (newId, promise.future)
  }

  def get(id: Int): Promise[T] = map(id)
}

/** Serializes, identifies and send a request over the given connection the promise associated
 *  with the returned future will be pending in the given PendingPromises.
 *  To be used with IdentifiedMessageListener and IdentifiedConnectionListener. */
class IdentifiedCallOverConnection(
      connection: ConnectionHandle, promises: PendingPromises[String])
      extends (Request[String] => Future[String]) {
        
  def apply(request: Request[String]): Future[String] = {
    val (id, future) = promises.next()
    connection.write(upickle.write(RequestWithId(request, id)))
    future
  }
  
}

/** Deserializes and identifies a response to complete the corresponding promise.
 *  To be used with IdentifiedCallOverConnection and IdentifiedConnectionListener. */
class IdentifiedMessageListener(promises: PendingPromises[String])(
      implicit ec: ExecutionContext) extends MessageListener {

  def notify(inboundPayload: String): Unit = {
    val identifiedResponse = upickle.read[ResponseWithId](inboundPayload)
    promises.get(identifiedResponse.id).success(identifiedResponse.res)
  }
  
}

/** Unwrap the identified request, do the actualCall and write an identified response.
 *  To be used with IdentifiedCallOverConnection and IdentifiedConnectionListener. */
class IdentifiedConnectionListener(actualCall: Request[String] => Future[String])(
      implicit ec: ExecutionContext) extends ConnectionListener {
  
  def notify(connection: ConnectionHandle): Unit = connection.handlerPromise.success {
    new MessageListener {
      def notify(pickle: String): Unit = {
        val identifiedRequest = upickle.read[RequestWithId](pickle)
        val result: Future[String] = actualCall(identifiedRequest.req)
        result.foreach { response =>
          connection.write(upickle.write(ResponseWithId(response, identifiedRequest.id)))
        }
      }
    }
  }
  
}

private case class RequestWithId(req: Request[String], id: Int)

private case class ResponseWithId(res: String, id: Int)
