package transport.play

import scala.concurrent._

import play.api.Application
import play.api.mvc._
import play.twirl.api.Html

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
  def action(): WebSocket[String, String] = WebSocket.tryAcceptWithActor[String, String] {
    BridgeActor.actionHandle(promise)
  }

  def listen(): Future[Promise[ConnectionListener]] =
    Future.successful(promise)
  
  def connect(remote: WebSocketUrl): Future[ConnectionHandle] =
    Future.failed(new UnsupportedOperationException(
      "Servers cannot initiate WebSockets connections."))
  
  def shutdown(): Unit = ()
}

object WebSocketServer {
  /** Generates a JavaScript route to a WebSocketServer. Use WebSocketClient.addressFromPlayRoute()
   *  to load the route as a WebSocketUrl on the client side. */
  def javascriptRoute(socketRoute: Call)(implicit request: RequestHeader) = Html {
    s"""var webSocketUrl = '${socketRoute.webSocketURL()}';"""
  }
}
