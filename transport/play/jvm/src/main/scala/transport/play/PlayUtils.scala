package transport.play

import play.api.mvc.{ RequestHeader, Call }
import play.sockjs.api.SockJSRouter
import play.twirl.api.Html

import transport._

object PlayUtils {
  /** Generates a JavaScript route to a WebSocketServer. Use WebSocketClient.addressFromPlayRoute()
   *  to load the route as a WebSocketUrl on the client side. */
  def webSocketRoute(socketRoute: Call)(implicit request: RequestHeader) = Html {
    val url = socketRoute.webSocketURL()
    s"""var webSocketUrl = '$url';"""
  }

  /** Generates a JavaScript route to a SockJSServer. Use SockJSClient.addressFromPlayRoute()
   *  to load the route as a SockJSUrl on the client side. */
  def sockJSRoute(router: SockJSRouter)(implicit request: RequestHeader) = Html {
    val url = Call("GET", router.prefix).absoluteURL()
    s"""var sockJSUrl = '$url';"""
  }
}
