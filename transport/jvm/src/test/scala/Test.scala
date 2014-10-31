package test

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import transport.client._
import transport._

object Test extends App {

  val sentMessage = "Hi!"
  val promise = Promise[String]()

  val ws = new WebSocketClient()
  val connection = ws.connect(WebSocketUrl("ws://echo.websocket.org"))
  
  connection.foreach { _.write(sentMessage) }
  connection.foreach { _.handlerPromise.success(new MessageListener {
    def notify(s: String): Unit = promise.success(s)
  })}
  
  val reviedMessage = Await.result(promise.future, 2.seconds)
  
  assert (sentMessage == reviedMessage)
  
}
