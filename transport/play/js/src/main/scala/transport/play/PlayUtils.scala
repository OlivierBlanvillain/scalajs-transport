package transport.play

import scala.scalajs.js
import transport._

object PlayUtils {
  /** Load the SockJSUrl defined in a play template. */
  def sockJSFromPlayRoute(): SockJSUrl = {
    try {
      SockJSUrl(js.Dynamic.global.sockJSUrl.asInstanceOf[String])
    } catch {
      case e: ClassCastException => // Or UndefinedBehaviorError?
        throw new RuntimeException("SockJSUrl not found. Make sure SockJSServer.javascriptRoute is included in the page template.")
    }
  }
  
  /** Load the WebSocketUrl defined in a play template. */
  def webSocketFromPlayRoute(): WebSocketUrl = {
    try {
      WebSocketUrl(js.Dynamic.global.webSocketUrl.asInstanceOf[String])
    } catch {
      case e: ClassCastException => // Or UndefinedBehaviorError?
        throw new RuntimeException("WebSocketUrl not found. Make sure WebSocketServer.javascriptRoute is included in the page template.")
    }
  }
}
