package transport.javascript

import transport._
import scala.concurrent._
import scala.util.{ Success, Failure }
import jsapi._

/** TODOC */
class WebSocketClient(implicit ec: ExecutionContext) extends WebSocketTransport {
  def listen(): Future[Promise[ConnectionListener]] =
    Future.failed(new UnsupportedOperationException(
      "Browsers can only initiate WebSockets connections."))
  
  def connect(remote: WebSocketUrl): Future[ConnectionHandle] = {
    val connectionPromise = Promise[ConnectionHandle]()
    
    new ConnectionHandle {
      val webSocket = new WebSocket(remote.url)
      val closePromise = Promise[Unit]()
      val promise = QueueablePromise[MessageListener]()
      
      webSocket.onopen = { event: Event =>
        connectionPromise.success(this)
      }
      webSocket.onmessage = { event: MessageEvent =>
        promise.queue(_(event.data.toString))
      }
      webSocket.onclose = { event: Event =>
        closePromise.success(())
      }
      webSocket.onerror = { event: Event =>
        // TODO: transmit this error to the listener?
        closePromise.success(())
      }
      
      def handlerPromise: Promise[MessageListener] = promise
      def closed: Future[Unit] = closePromise.future
      def write(outboundPayload: String): Unit = webSocket.send(outboundPayload)
      def close(): Unit = webSocket.close()
    }
    
    connectionPromise.future
  }
  
  def shutdown(): Unit = ()
}

object WebSocketClient {
  /** Load the WebSocketUrl defined in a play template. */
  def addressFromPlayRoute(): WebSocketUrl = {
    try {
      WebSocketUrl(scala.scalajs.js.Dynamic.global.webSocketUrl.asInstanceOf[String])
    } catch {
      case e: ClassCastException =>
        throw new RuntimeException("WebSocketUrl not found. Make sure WebSocketServer.javascriptRoute is included in the page template.")
    }
  }
}
