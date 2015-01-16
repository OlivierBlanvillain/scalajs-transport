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

// Shared
trait Api {
  def doThing(i: Int, s: String): Seq[String]
}
class MyRpcWrapper[T <: Transport](t: T)(implicit ec: ExecutionContext)
     extends RpcWrapper[T, upickle.Reader, upickle.Writer](t: T)(ec) {
  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}
  
// Server Side
object Server extends Api {
  def doThing(i: Int, s: String) = Seq.fill(i)(s)
}
val transport = new WebSocketServer(8080, "/ws")
new MyRpcWrapper(transport).serve(_.route[Api](Server))
  
// Client Side
val abstracttransport = new WebSocketClient()
val url = WebSocketUrl("ws://localhost:8080/ws")
val client = new MyRpcWrapper(abstracttransport).connect(url)
val result: Future[Seq[String]] = client[Api].doThing(3, "ha").call()

}/**/
