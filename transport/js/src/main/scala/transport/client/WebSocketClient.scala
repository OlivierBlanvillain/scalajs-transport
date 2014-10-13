package transport.client

import transport._
import scala.concurrent._
import scala.util.{ Success, Failure }
import jsapi._

class WebSocketClient(implicit executionContext: ExecutionContext) extends WebSocketTransport {
  override def listen(): Future[Promise[ConnectionListener]] =
    Future.failed(new UnsupportedOperationException(
      "Browsers can only initiate WebSockets connections."))
  
  override def connect(remote: WebSocketUrl): Future[ConnectionHandle] = {
    val connectionPromise = Promise[ConnectionHandle]()
    
    new ConnectionHandle {
      val webSocket = new WebSocket(remote.url)
      val promise = Promise[MessageListener]()
      private var poorMansBuffer: Future[MessageListener] = promise.future
      
      webSocket.addEventListener("open", { (event: Event) =>
        connectionPromise.success(this)
      }, useCapture = false)
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
        // TODO: transmit this error to the listener.
        poorMansBuffer = poorMansBuffer.andThen {
          case Success(listener) =>
            listener.closed()
        }
      }, useCapture = false)
      
      override def handlerPromise: Promise[MessageListener] = promise
      
      override def write(outboundPayload: String): Unit = webSocket.send(outboundPayload)
      
      override def close(): Unit = webSocket.close()
    }
    
    connectionPromise.future
  }
  
  override def shutdown(): Unit = ()
}
object WebSocketClient {
  def addressFromPlayTemplate: WebSocketUrl =
    WebSocketUrl(scala.scalajs.js.Dynamic.global.webSocketUrl.asInstanceOf[String])
}
