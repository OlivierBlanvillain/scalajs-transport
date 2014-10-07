package transport.websocket

import transport._
import ConnectionHandle._
import scala.concurrent._
import scala.util.{ Success, Failure }
import jsapi._

class WebSocketClient(implicit executionContext: ExecutionContext) extends WebSocketTransport {
  override def listen(): Future[(WebSocketUrl, Promise[Transport.ConnectionListener])] = {
    Future.failed(new UnsupportedOperationException(
      "Browsers can only initiate WebSockets connections."))
  }
  
  override def connect(remote: WebSocketUrl): Future[ConnectionHandle] = {
    // TODO: Check for connection initialisation.
    Future.successful(
      new ConnectionHandle {
        val webSocket = new WebSocket(remote.url)
        val promise: Promise[MessageListener] = Promise()
        private var poorMansBuffer: Future[MessageListener] = promise.future
        
        webSocket.addEventListener("message", { (event: Event) =>
          poorMansBuffer = poorMansBuffer.andThen {
            case Success(listener) =>
              listener.notify(event.asInstanceOf[MessageEvent].data.toString())
          }
        }, useCapture = false)
        webSocket.addEventListener("close", { (event: Event) =>
          poorMansBuffer = poorMansBuffer.andThen {
            case Success(listener) =>
              listener.closed()
          }
        }, useCapture = false)
        webSocket.addEventListener("error", { (event: Event) =>
          poorMansBuffer = poorMansBuffer.andThen {
            case Success(listener) =>
              listener.closed()
          }
        }, useCapture = false)
        
        def handlerPromise: Promise[MessageListener] = promise
        
        def write(outboundPayload: String): Unit = webSocket.send(outboundPayload)
        
        def close(): Unit = webSocket.close()
      }
    )
  }
  
  override def shutdown(): Future[Unit] =
    Future.successful(())
}
