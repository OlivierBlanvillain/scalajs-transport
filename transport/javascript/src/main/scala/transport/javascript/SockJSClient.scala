package transport.javascript

import transport._
import scala.concurrent._
import scala.util.{ Success, Failure }
import jsapi._

/** TODOC */
class SockJSClient(implicit ec: ExecutionContext) extends SockJSTransport {
  def listen(): Future[Promise[ConnectionListener]] =
    Future.failed(new UnsupportedOperationException(
      "Browsers can only initiate SockJSs connections."))
  
  def connect(remote: SockJSUrl): Future[ConnectionHandle] = {
    val connectionPromise = Promise[ConnectionHandle]()
    
    new ConnectionHandle {
      val sockJS = new SockJS(remote.url)
      val promise = QueueablePromise[MessageListener]()
      val closePromise = Promise[Unit]()
      
      sockJS.onopen = { event: Event =>
        connectionPromise.success(this)
      }
      sockJS.onmessage = { event: MessageEvent =>
        promise.queue(_(event.data.toString))
      }
      sockJS.onclose = { event: Event =>
        closePromise.success(())
      }
      sockJS.onerror = { event: Event =>
        // TODO: transmit this error to the listener?
        closePromise.success(())
      }
      
      def handlerPromise: Promise[MessageListener] = promise
      def closed: Future[Unit] = closePromise.future
      def write(outboundPayload: String): Unit = sockJS.send(outboundPayload)
      def close(): Unit = sockJS.close()
    }
    
    connectionPromise.future
  }
  
  def shutdown(): Unit = ()
}

object SockJSClient {
  /** Load the SockJSUrl defined in a play template. */
  def addressFromPlayRoute(): SockJSUrl = {
    try {
      SockJSUrl(scala.scalajs.js.Dynamic.global.sockJSUrl.asInstanceOf[String])
    } catch {
      case e: ClassCastException =>
        throw new RuntimeException("SockJSUrl not found. Make sure SockJSServer.javascriptRoute is included in the page template.")
    }
  }
}
