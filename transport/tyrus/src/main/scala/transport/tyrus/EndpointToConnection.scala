package transport.tyrus

import scala.concurrent._
import scala.util.{ Success, Failure }

import transport._
import javax.websocket._

private class EndpointToConnection(connectionPromise: Promise[ConnectionHandle])(
      implicit ec: ExecutionContext) extends Endpoint {
  val closePromise = Promise[Unit]()
  val promise = QueueablePromise[MessageListener]()
  
  override def onOpen(session: Session, config: EndpointConfig): Unit = {
    session.addMessageHandler(new MessageHandler.Whole[String]() {
      override def onMessage(message: String): Unit = {
        promise.queue(_(message))
      }
    })
    connectionPromise.success(
      new ConnectionHandle {
        def closed: Future[Unit] = closePromise.future
        def handlerPromise: Promise[MessageListener] = promise
        def write(m: String): Unit = session.getBasicRemote().sendText(m)
        def close(): Unit = session.close()
      }
    )
  }
  override def onClose(session: Session, closeReason: CloseReason): Unit = {
    closePromise.success(())
  }
  override def onError(session: Session, thr: Throwable): Unit = {
    // TODO: transmit this error to the listener?
    closePromise.success(())
  }
}
object EndpointToConnection {
  def apply()(implicit ec: ExecutionContext): (Endpoint, Future[ConnectionHandle]) = {
    val promise = Promise[ConnectionHandle]()
    val endpoint = new EndpointToConnection(promise)
    (endpoint, promise.future)
  }
}
