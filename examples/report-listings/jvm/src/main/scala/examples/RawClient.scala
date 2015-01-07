package examples

import transport._
import transport.tyrus._

import scala.concurrent.ExecutionContext.Implicits.global

object RawClient { /**/

val client = new WebSocketClient()
val url = WebSocketUrl("ws://echo.websocket.org")
  
client.connect(url) foreach { connectionHandle =>
  connectionHandle.handlerPromise.success { message =>
    print("Recived: " + message)
  }
  connectionHandle.write("Hello WebSocket!")
}
  
} /**/
