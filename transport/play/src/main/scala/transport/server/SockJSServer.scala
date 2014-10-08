package transport.server

import scala.concurrent._

import play.api.Application
import play.api.mvc._
import play.sockjs.api._

import transport._

case class SockJSServer()(implicit ec: ExecutionContext, app: Application)
    extends SockJSTransport {
  private val promise = Promise[ConnectionListener]()
  
  /** Method to be called by the play controller for each new connection to socketRoute.
   *  
   *  For example,
   *  {{{
   *  // In conf/routes:
   *  ->      /socket                     controllers.Application.socket
   *  
   *  // In controllers.Application:
   *  val transport = SockJSServer(routes.Application.socket)
   *  def socket = transport.action()
   *  }}}
   */
  def action(): SockJSRouter = SockJSRouter.tryAcceptWithActor[String, String] {
    BridgeActor.actionHandle(promise)
  }
  
  override def listen(): Future[(SockJSUrl, Promise[ConnectionListener])] = {
    Future.successful((SockJSUrl("TODO"), promise))
  }
  
  override def connect(remote: SockJSUrl): Future[ConnectionHandle] =
    Future.failed(new UnsupportedOperationException(
      "Servers cannot initiate SockJSs connections."))
  
  override def shutdown(): Future[Unit] =
    Future.successful(())
}
