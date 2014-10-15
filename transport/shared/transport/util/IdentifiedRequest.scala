package transport.util

import autowire._
import upickle._
import scala.concurrent._
import transport._
import autowire.Core.Request

case class connectionSomethingClient(connection: Future[ConnectionHandle])(
      implicit ec: ExecutionContext) {
  
  var pendingPromise: Option[Promise[String]] = None
  
  connection.foreach { _.handlerPromise.success(
    new MessageListener {
      def notify(inboundPayload: String): Unit = {
        // TODO: Ain't gonna work for interleaved method calls.
        pendingPromise.foreach { _.success(inboundPayload) }
        pendingPromise = None
      }
      def closed(): Unit = ()
    }
  )}
  
  def doCall(request: Request[String]): Future[String] = {
    connection.foreach { _.write(upickle.write(request))}
    pendingPromise = Some(Promise())
    pendingPromise.get.future
    // val (future, identifiedRequest) = somemagicobject.next(request)
    // connection.foreach { _.write(upickle.write(identifiedRequest)) }
    // future
  }

  // object somemagicobject {
  //   def next(request: Request[String]): (Future[String], IdentifiedRequest) = ???
  //   def success = ???
  // }
}

case class magicConnectionListener(doreq: Request[String] => Future[String])(
      implicit ec: ExecutionContext) extends ConnectionListener {
  override def notify(connection: ConnectionHandle): Unit = {
    connection.handlerPromise.success {
      new MessageListener {
        override def notify(pickle: String): Unit = {
          val request: Request[String] = upickle.read[Request[String]](pickle)
          val result: Future[String] = doreq(request)
          result.foreach { connection write _ }
        }
        override def closed(): Unit = ()
      }
    }
  }
}

// // case class IdentifiedRequest(request: Request[String], id: Int)

// // case class IdentifiedResponse(response: String, id: Int)

