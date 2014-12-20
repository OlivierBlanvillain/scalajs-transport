package transport.rpc

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

import autowire._
import upickle._

import transport._
import transport.tyrus._
import transport.rpc._

object ClientSide { /**/

// Client Side
val transport = new WebSocketClient()
val url = WebSocketUrl("http://localhost:8080/ws")
val client = new RpcWrapper(transport).connect(url)
val result: Future[Seq[String]] =
    client[Api].doThing(3, "ha").call()

}/**/
