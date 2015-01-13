package shared

import transport._
import transport.rpc._
import scala.concurrent.ExecutionContext
import upickle._

trait Api {
  def list(path: String): Seq[String]
  def double(i: Int): Int
}

class MyRpcWrapper[T <: Transport](t: T)(implicit ec: ExecutionContext)
     extends RpcWrapper[T, Reader, Writer](t: T)(ec) {
  def read[Result: Reader](p: String) = upickle.read[Result](p)
  def write[Result: Writer](r: Result) = upickle.write(r)
}
