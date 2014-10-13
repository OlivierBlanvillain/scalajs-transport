package transport.server

import scala.concurrent._

import play.api.Application
import play.api.mvc._

import transport._

case class WebSocketServer(implicit ec: ExecutionContext, app: Application)
    extends WebSocketTransport {
  private val promise = Promise[ConnectionListener]()
  
  /** Method to be called by the play controller for each new connection to socketRoute.
   *  
   *  For example,
   *  {{{
   *  // In conf/routes:
   *  GET     /socket                     controllers.Application.socket
   *  
   *  // In controllers.Application:
   *  val transport = WebSocketServer(routes.Application.socket)
   *  def socket = transport.action()
   *  }}}
   */
  def action(): WebSocket[String, String] = WebSocket.tryAcceptWithActor[String, String] {
    BridgeActor.actionHandle(promise)
  }
  
  override def listen(): Future[Promise[ConnectionListener]] =
    Future.successful(promise)
  
  override def connect(remote: WebSocketUrl): Future[ConnectionHandle] =
    Future.failed(new UnsupportedOperationException(
      "Servers cannot initiate WebSockets connections."))
  
  override def shutdown(): Unit = ()
}
