package transport.rpc

import scala.concurrent._
import transport._
import transport.netty._
import transport.tyrus._
import transport.rpc._
import autowire._
import upickle._
import scala.concurrent.ExecutionContext.Implicits.global

/* abstract /* */
* class RpcWrapper[T <: Transport](t: T) {
*   type StringServer = Server[String, Reader, Writer]
*   type StringClient = Client[String, Reader, Writer]
*   type Rpc = Request[String] => Future[String]
*
*   def serve(routeMacro: StringServer => Rpc): Unit
*   def connect(address: t.Address): StringClient
* }
*/

object Demo { /**/

  // Shared API
  trait Api {
    def doThing(i: Int, s: String): Seq[String]
  }

  // Server-side implementation
  object Server extends Api {
    def doThing(i: Int, s: String) = Seq.fill(i)(s)
  }
  val transport = new WebSocketServer(8080, "/ws")
  new RpcWrapper(transport).serve(_.route[Api](Server))

  // Client-side
  val url = WebSocketUrl("http://localhost:8080/ws")
  val client = new RpcWrapper(new WebSocketClient()).connect(url)

  val result: Future[Seq[String]] = client[Api].doThing(3, "ha").call()
  result.foreach(println) // List(ha, ha, ha)

}/**/
