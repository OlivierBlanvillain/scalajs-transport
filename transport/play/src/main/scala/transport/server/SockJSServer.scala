package transport.server

import scala.concurrent._

import play.api.Application
import play.sockjs.api._
import play.twirl.api.Html

import transport._

case class SockJSServer(implicit ec: ExecutionContext, app: Application)
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
  
  override def listen(): Future[Promise[ConnectionListener]] =
    Future.successful(promise)
  
  override def connect(remote: SockJSUrl): Future[ConnectionHandle] =
    Future.failed(new UnsupportedOperationException(
      "Servers cannot initiate SockJSs connections."))
  
  override def shutdown(): Unit = ()
}

object SockJSServer {
  /** Generates a JavaScript route to a SockJSServer. Use SockJSClient.addressFromPlayRoute
   *  to load the route as a SockJSUrl on the client side. */
  def javascriptRoute(router: SockJSRouter)(implicit request: play.api.mvc.RequestHeader) = Html {
    s"""var sockJSUrl = '${play.api.mvc.Call("GET", router.prefix).absoluteURL()}';"""
  }
}
