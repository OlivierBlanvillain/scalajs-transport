package examples

import transport._
import transport.tyrus._

import scala.concurrent.ExecutionContext.Implicits.global

object RawClient { /**/

val transport = new WebSocketClient()
val url = WebSocketUrl("ws://echo.websocket.org")
val futureConnectionHandle = transport.connect(url)
  
futureConnectionHandle foreach { connection =>
  connection.write("Hello WebSocket!")
  connection.handlerPromise.success { message =>
    print("Recived: " + message)
    connection.close()
  }
}
  
/*futureConnectionHandle onFailure { case exception =>*/
/*  println("Something went wrong: " + exception)*/
/*}*/
} /**/

