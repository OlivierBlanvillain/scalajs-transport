package transport.rpc

import scala.concurrent._
import transport._
import autowire.Core.Request
import autowire._
import scala.language.higherKinds

/** TODOC */
abstract class RpcWrapper[T <: Transport, Reader[_], Writer[_]](
      transport: T)(
      implicit ec: ExecutionContext) extends StringSerializers[Reader, Writer] {

  self =>
  
  type StringServer = autowire.Server[String, Reader, Writer]
  type StringClient = autowire.Client[String, Reader, Writer]
  
  /** TODOC */
  def serve(routeMacro: StringServer => (Request[String] => Future[String])): Unit = {
    object AutowireServer extends StringServer {
      def read[Result: Reader](p: String) = self.read[Result](p)
      def write[Result: Writer](r: Result) = self.write(r)
    }
    
    transport.listen().map { promise =>
      promise.success(new IdentifiedConnectionListener(routeMacro(AutowireServer)))
    }
  }
  
  /** TODOC */
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
