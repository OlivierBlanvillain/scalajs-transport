package transport.client

import transport._
import scala.concurrent._
import scala.util.{ Success, Failure }
import jsapi._

class WebSocketClient(implicit ec: ExecutionContext) extends WebSocketTransport {
  override def listen(): Future[Promise[ConnectionListener]] =
    Future.failed(new UnsupportedOperationException(
      "Browsers can only initiate WebSockets connections."))
  
  override def connect(remote: WebSocketUrl): Future[ConnectionHandle] = {
    val connectionPromise = Promise[ConnectionHandle]()
    
    new ConnectionHandle {
      val webSocket = new WebSocket(remote.url)
      val promise = Promise[MessageListener]()
      private var poorMansBuffer: Future[MessageListener] = promise.future
      
      webSocket.onopen = { (event: Event) =>
        connectionPromise.success(this)
      }
      webSocket.onmessage = { (event: MessageEvent) =>
        poorMansBuffer = poorMansBuffer.andThen {
          case Success(listener) =>
            listener.notify(event.data.toString())
        }
      }
      webSocket.onclose = { (event: Event) =>
        poorMansBuffer = poorMansBuffer.andThen {
          case Success(listener) =>
            listener.closed()
        }
      }
      webSocket.onerror = { (event: Event) =>
        // TODO: transmit this error to the listener.
        poorMansBuffer = poorMansBuffer.andThen {
          case Success(listener) =>
            listener.closed()
        }
      }
      
      override def handlerPromise: Promise[MessageListener] = promise
      
      override def write(outboundPayload: String): Unit = webSocket.send(outboundPayload)
      
      override def close(): Unit = webSocket.close()
    }
    
    connectionPromise.future
  }
  
  override def shutdown(): Unit = ()
}
object WebSocketClient {
  // TODO: Nice error message if the address is not here...
  /** Load the WebSocketUrl defined in a play template. */
  def addressFromPlayRoute(): WebSocketUrl =
    WebSocketUrl(scala.scalajs.js.Dynamic.global.webSocketUrl.asInstanceOf[String])
}
