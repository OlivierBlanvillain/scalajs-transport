package transport.play

import scala.concurrent._

import play.api.Application
import play.sockjs.api._
import play.twirl.api.Html
import play.api.mvc.{ Call, RequestHeader }

import transport._

/** SockJS server based on the [[https://github.com/fdimuccio/play2-sockjs play2-sockjs]] plugin.
 *  
 *  Incomming connections have to be passed to SockJSServer using its .action() method.
 *
 *  For example,
 *  {{{
 *  // In conf/routes:
 *  ->      /socket                     controllers.Application.socket
 *  
 *  // In controllers.Application:
 *  val transport = SockJSServer()
 *  val socket = transport.action()
 *  }}}
 */
class SockJSServer(implicit ec: ExecutionContext, app: Application)
    extends SockJSTransport {
  private val promise = Promise[ConnectionListener]()
  
  /** Method to be called from a play controller for each new SockJS connection. */
  def action(): SockJSRouter = SockJSRouter.tryAcceptWithActor[String, String] {
    BridgeActor.actionHandle(promise)
  }
  
  def listen(): Future[Promise[ConnectionListener]] =
    Future.successful(promise)
  
  def connect(remote: SockJSUrl): Future[ConnectionHandle] =
    Future.failed(new UnsupportedOperationException(
      "Servers cannot initiate SockJSs connections."))
  
  def shutdown(): Unit = ()
}

object SockJSServer {
  /** Generates a JavaScript route to a SockJSServer. Use SockJSClient.addressFromPlayRoute()
   *  to load the route as a SockJSUrl on the client side. */
  def javascriptRoute(router: SockJSRouter)(implicit request: RequestHeader) = Html {
    s"""var sockJSUrl = '${Call("GET", router.prefix).absoluteURL()}';"""
  }
}
