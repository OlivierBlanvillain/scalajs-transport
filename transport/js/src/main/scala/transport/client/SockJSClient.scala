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
      
      sockJS.addEventListener("open", { (event: Event) =>
        connectionPromise.success(this)
      }, useCapture = false)
      sockJS.addEventListener("message", { (event: Event) =>
        poorMansBuffer = poorMansBuffer.andThen {
          case Success(listener) =>
            listener.notify(event.asInstanceOf[MessageEvent].data.toString())
        }
      }, useCapture = false)
      sockJS.addEventListener("close", { (event: Event) =>
        poorMansBuffer = poorMansBuffer.andThen {
          case Success(listener) =>
            listener.closed()
        }
      }, useCapture = false)
      sockJS.addEventListener("error", { (event: Event) =>
        // TODO: transmit this error to the listener.
        poorMansBuffer = poorMansBuffer.andThen {
          case Success(listener) =>
            listener.closed()
        }
      }, useCapture = false)
      
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
  def addressFromPlayRoute: SockJSUrl =
    SockJSUrl(scala.scalajs.js.Dynamic.global.sockJSUrl.asInstanceOf[String])
}
