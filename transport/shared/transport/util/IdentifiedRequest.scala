package transport.util

import scala.collection.mutable
import autowire._
import upickle._
import scala.concurrent._
import transport._
import autowire.Core.Request

case class connectionSomethingClient(connection: Future[ConnectionHandle])(
      implicit ec: ExecutionContext) {
  
  val magic = new SomeMagicObject[String]()
  
  connection.foreach { _.handlerPromise.success(
    new MessageListener {
      def notify(inboundPayload: String): Unit = {
        val identifiedResponse = upickle.read[IdentifiedResponse](inboundPayload)
        magic.gett(identifiedResponse.id).success(identifiedResponse.res)
      }
    }
  )}
  
  def doCall(request: Request[String]): Future[String] = {
    val (future, id) = magic.nextt()
    connection.foreach { _.write(upickle.write(IdentifiedRequest(request, id))) }
    future
  }

  class SomeMagicObject[T] {
    private var id = 0
    private val map = mutable.Map.empty[Int, Promise[T]]
    
    def nextt(): (Future[T], Int) = {
      val p = Promise[T]()
      id = id + 1
      map += ((id, p))
      (p.future, id)
    }
    def gett(id: Int): Promise[T] = {
      val p = map(id)
      map -= id
      p
    }
  }
}

// Server
case class magicConnectionListener(doreq: Request[String] => Future[String])(
      implicit ec: ExecutionContext) extends ConnectionListener {
  override def notify(connection: ConnectionHandle): Unit = {
    connection.handlerPromise.success {

      new MessageListener {
        override def notify(pickle: String): Unit = {
          val identifiedRequest = upickle.read[IdentifiedRequest](pickle)
          val result: Future[String] = doreq(identifiedRequest.req)
          result.foreach { response =>
            connection.write(upickle.write(IdentifiedResponse(response, identifiedRequest.id)))
          }
        }
      }

    }
  }
}

case class IdentifiedRequest(req: Request[String], id: Int)

case class IdentifiedResponse(res: String, id: Int)
