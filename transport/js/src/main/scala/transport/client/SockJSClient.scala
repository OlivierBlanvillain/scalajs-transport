package transport.client

import transport._
import scala.concurrent._
import scala.util.{ Success, Failure }
import jsapi._

class SockJSClient(implicit executionContext: ExecutionContext) extends SockJSTransport {
  override def listen(): Future[Promise[ConnectionListener]] =
    Future.failed(new UnsupportedOperationException(
      "Browsers can only initiate SockJSs connections."))
  
  override def connect(remote: SockJSUrl): Future[ConnectionHandle] = {
    val connectionPromise = Promise[ConnectionHandle]()
    
    new ConnectionHandle {
      val sockJS = new SockJS(remote.url)
      val promise = Promise[MessageListener]()
      private var poorMansBuffer: Future[MessageListener] = promise.future
      
      sockJS.onopen = { (event: Event) =>
        connectionPromise.success(this)
      }
      sockJS.onmessage = { (event: MessageEvent) =>
        poorMansBuffer = poorMansBuffer.andThen {
          case Success(listener) =>
            listener.notify(event.data.toString())
        }
      }
      sockJS.onclose = { (event: Event) =>
        poorMansBuffer = poorMansBuffer.andThen {
          case Success(listener) =>
            listener.closed()
        }
      }
      sockJS.onerror = { (event: Event) =>
        // TODO: transmit this error to the listener.
        poorMansBuffer = poorMansBuffer.andThen {
          case Success(listener) =>
            listener.closed()
        }
      }
      
      override def handlerPromise: Promise[MessageListener] = promise
      
      override def write(outboundPayload: String): Unit = sockJS.send(outboundPayload)
      
      override def close(): Unit = sockJS.close()
    }
    
    connectionPromise.future
  }
  
  override def shutdown(): Unit = ()
}
object SockJSClient {
  /** Load the SockJSUrl defined in a play template. */
  def addressFromPlayRoute(): SockJSUrl =
    SockJSUrl(scala.scalajs.js.Dynamic.global.sockJSUrl.asInstanceOf[String])
}
