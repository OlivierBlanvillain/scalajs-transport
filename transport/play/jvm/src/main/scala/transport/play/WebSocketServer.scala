package transport.play

import scala.concurrent._

import play.api.Application
import play.api.mvc._

import transport._

/** WebSocket server based on the native Play WebSocket support.
 *  
 *  Incomming connections have to be passed to WebSocketServer using its .action() method.
 *
 *  For example,
 *  {{{
 *  // In conf/routes:
 *  GET     /socket                     controllers.Application.socket
 *  
 *  // In controllers.Application:
 *  val transport = WebSocketServer()
 *  def socket = transport.action()
 *  }}}
 */
class WebSocketServer(implicit ec: ExecutionContext, app: Application)
    extends WebSocketTransport {
  private val promise = Promise[ConnectionListener]()
  
  /** Method to be called from a play controller for each new WebSocket connection. */
  def action(authorise: RequestHeader => Boolean = _ => true): WebSocket[String, String] = {
    WebSocket.tryAcceptWithActor[String, String] {
      BridgeActor.actionHandle(promise, authorise)
    }
  }
  
  def listen(): Future[Promise[ConnectionListener]] =
    Future.successful(promise)
  
  def connect(remote: WebSocketUrl): Future[ConnectionHandle] =
    Future.failed(new UnsupportedOperationException(
      "Servers cannot initiate WebSockets connections."))
  
  def shutdown(): Future[Unit] = Future.successful(Unit)
}
