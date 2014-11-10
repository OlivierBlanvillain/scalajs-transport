package transport.play

import scala.concurrent._

import play.api.Application
import play.sockjs.api._
import play.twirl.api.Html
import play.api.mvc.{ Call, RequestHeader }

import transport._

class SockJSServer(implicit ec: ExecutionContext, app: Application)
    extends SockJSTransport {
  private val promise = Promise[ConnectionListener]()
  
  /** Method to be called by the play controller for each new connection to SockJSRouter.
   *  
   *  For example,
   *  {{{
   *  // In conf/routes:
   *  ->      /socket                     controllers.Application.socket
   *  
   *  // In controllers.Application:
   *  val transport = SockJSServer()
   *  def socket = transport.action()
   *  }}}
   */
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
  def apply()(implicit ec: ExecutionContext, app: Application) = new SockJSServer()

  /** Generates a JavaScript route to a SockJSServer. Use SockJSClient.addressFromPlayRoute()
   *  to load the route as a SockJSUrl on the client side. */
  def javascriptRoute(router: SockJSRouter)(implicit request: RequestHeader) = Html {
    s"""var sockJSUrl = '${Call("GET", router.prefix).absoluteURL()}';"""
  }
}
