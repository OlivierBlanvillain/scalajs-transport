package transport.rpc

import scala.concurrent.ExecutionContext.Implicits.global

import autowire._
import upickle._

import transport._
import transport.netty._
import transport.rpc._

object ServerSide { /**/

// Server Side
object Server extends Api {
  def doThing(i: Int, s: String) = Seq.fill(i)(s)
}
val transport = new WebSocketServer(8080, "/ws")
new RpcWrapper(transport).serve(_.route[Api](Server))

}  /**/
