package transport.client

import scala.concurrent._
import scala.util.{ Success, Failure }

import transport._
import javax.websocket._

private class EndpointToConnection(connectionPromise: Promise[ConnectionHandle])(
      implicit ec: ExecutionContext) extends Endpoint {
  val promise = Promise[MessageListener]()
  private var poorMansBuffer: Future[MessageListener] = promise.future
  
  override def onOpen(session: Session, config: EndpointConfig): Unit = {
    session.addMessageHandler(new MessageHandler.Whole[String]() {
      override def onMessage(message: String): Unit = {
        poorMansBuffer = poorMansBuffer.andThen {
          case Success(listener) =>
            listener.notify(message)
        }
      }
    })
    connectionPromise.success(
      new ConnectionHandle {
        def handlerPromise: Promise[MessageListener] = promise
        def write(m: String): Unit = session.getBasicRemote().sendText(m)
        def close(): Unit = session.close()
      }
    )
  }
  override def onClose(session: Session, closeReason: CloseReason): Unit = {
    poorMansBuffer = poorMansBuffer.andThen {
      case Success(listener) =>
        listener.closed()
    }
  }
  override def onError(session: Session, thr: Throwable): Unit = {
    // TODO: transmit this error to the listener.
    poorMansBuffer = poorMansBuffer.andThen {
      case Success(listener) =>
        listener.closed()
    }
  }
}
object EndpointToConnection {
  def apply()(implicit ec: ExecutionContext): (Endpoint, Future[ConnectionHandle]) = {
    val promise = Promise[ConnectionHandle]()
    val endpoint = new EndpointToConnection(promise)
    (endpoint, promise.future)
  }
}
