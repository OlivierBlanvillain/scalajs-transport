package transport.rpc

import scala.concurrent._
import transport._
import autowire.Core.Request
import autowire._
import scala.language.higherKinds

/** Wraps a `Transport` for remote procedure calls. Given a shared interface, the sever side should
 *  Implement this interface and serve it with a `Transport`. The client side can then connection 
 *  to this server and consume this interface. `RpcWrapper` implementation should implement `read`
 *  and `write` methods for serialization. Follows and example of `RpcWrapper` subclass for
 *  serialization using uPickle, to be shared between client and server:
 *  
 *  {{{
 *  class MyRpcWrapper[T <: Transport](t: T)(implicit ec: ExecutionContext)
 *       extends RpcWrapper[T, upickle.Reader, upickle.Writer](t: T)(ec) {
 *    def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
 *    def write[Result: upickle.Writer](r: Result) = upickle.write(r)
 *  }
 *  }}} 
 *  
 *  Example of usage:
 *  
 *  {{{
 *  // Shared
 *  trait Api {
 *    def doThing(i: Int, s: String): Seq[String]
 *  }
 *    
 *  // Server Side
 *  object Server extends Api {
 *    def doThing(i: Int, s: String) = Seq.fill(i)(s)
 *  }
 *  val transport = new WebSocketServer(8080, "/ws")
 *  new MyRpcWrapper(transport).serve(_.route[Api](Server))
 *    
 *  // Client Side
 *  val abstracttransport = new WebSocketClient()
 *  val url = WebSocketUrl("ws://localhost:8080/ws")
 *  val client = new MyRpcWrapper(abstracttransport).connect(url)
 *  val result: Future[Seq[String]] = client[Api].doThing(3, "ha").call()
 *  }}} 
 */
abstract class RpcWrapper[T <: Transport, Reader[_], Writer[_]](
      transport: T)(
      implicit ec: ExecutionContext) extends StringSerializers[Reader, Writer] {

  self =>
  
  type StringServer = autowire.Server[String, Reader, Writer]
  type StringClient = autowire.Client[String, Reader, Writer]
  
  /** Serves an implementation of the shared interface. */
  def serve(routeMacro: StringServer => (Request[String] => Future[String])): Unit = {
    object AutowireServer extends StringServer {
      def read[Result: Reader](p: String) = self.read[Result](p)
      def write[Result: Writer](r: Result) = self.write(r)
    }
    
    transport.listen().map { promise =>
      promise.success(new IdentifiedConnectionListener(routeMacro(AutowireServer)))
    }
  }
  
  /** Connect to a server implementing the shared interface. */
  def connect(
        address: transport.Address,
        pp: PendingPromises[String] = new PendingPromises[String]())
      : StringClient = {
        
    val futureConnection = transport.connect(address)
    
    futureConnection.foreach { _.handlerPromise.success(
      new IdentifiedMessageListener(pp)
    )}
    
    new StringClient {
      def doCall(request: Request): Future[String] = futureConnection.flatMap {
        new IdentifiedCallOverConnection(_, pp)(request)
      }
      
      def read[Result: Reader](p: String) = self.read[Result](p)
      def write[Result: Writer](r: Result) = self.write(r)
    }
  }
}

trait StringSerializers[Reader[_], Writer[_]] {
  def read[Result: Reader](p: String): Result
  def write[Result: Writer](r: Result): String
}
