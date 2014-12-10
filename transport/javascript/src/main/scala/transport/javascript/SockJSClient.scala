package transport.javascript

import transport._
import scala.concurrent._
import scala.util.{ Success, Failure }
import scala.scalajs.js
import jsapi._

/** SockJS JavaScript client. 
 *  
 *  Usage example:
 *  {{{
 *  new SockJSClient().connect(url).foreach { connection =>
 *    connection.handlerPromise.success { string => println("Recived: " + string) }
 *    connection.write("Hello SockJS!")
 *  }
 *  }}}
 *  
 *  If using the Scala.js sbt plugin to manage JavaScript dependencies, add your in build.sbt:
 *  {{{
 *  jsDependencies += "org.webjars" % "sockjs-client" % "0.3.4" / "sockjs.min.js"
 *  }}}
 */
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
      sockJS.onclose = { event: CloseEvent =>
        closePromise.trySuccess(())
      }
      sockJS.onerror = { event: ErrorEvent =>
        val message = try { event.message.toString } catch { case e: ClassCastException => "" }
        closePromise.tryFailure(SockJSException(message))
      }
      
      def handlerPromise: Promise[MessageListener] = promise
      def closedFuture: Future[Unit] = closePromise.future
      def write(outboundPayload: String): Unit = sockJS.send(outboundPayload)
      def close(): Unit = sockJS.close()
    }
    
    connectionPromise.future
  }
  
  def shutdown(): Future[Unit] = Future.successful(Unit)
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
