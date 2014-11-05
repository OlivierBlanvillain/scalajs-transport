package test

import org.scalatest._

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import transport.client._
import transport.server._
import transport._

class TestServer extends FlatSpec with Matchers {
  
  "WebSocketServer" should "serve websocket" in {
    val message = "hello"
    
    val path = "/ws"
    val port = 8080
    val server = new WebSocketServer(port, path)
    
    try {
      
      server.listen().foreach(_.success(new ConnectionListener {
        def notify(connection: ConnectionHandle): Unit = {
          connection.handlerPromise.success(new MessageListener {
            def notify(m: String): Unit = {
              connection.write(m.toUpperCase)
            }
          })
        }
      }))
      
      val promise = Promise[String]()

      val client = new WebSocketClient()
      val connection = client.connect(WebSocketUrl(s"ws://localhost:$port$path"))
      connection.foreach { _.handlerPromise.success(new MessageListener {
        def notify(s: String): Unit = promise.success(s)
      })}
      connection.foreach { _.write(message) }
      
      val reviedMessage = Await.result(promise.future, 2.seconds)
      
      assert (message.toUpperCase == reviedMessage)
      
    } finally server.shutdown()
  }
  
}
