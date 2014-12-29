package transport.rpc

import upickle._
import scala.concurrent._
import transport._
import autowire.Core.Request

/** TODOC */
class RpcWrapper[T <: Transport](
      transport: T)(
      implicit ec: ExecutionContext) {
  
  type StringServer = autowire.Server[String, upickle.Reader, upickle.Writer]
  type StringClient = autowire.Client[String, upickle.Reader, upickle.Writer]
  
  /** TODOC */
  def serve(routeMacro: StringServer => (Request[String] => Future[String])): Unit = {
    object AutowireServer extends StringServer {
      def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
      def write[Result: upickle.Writer](r: Result) = upickle.write(r)
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
      
      def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
      def write[Result: upickle.Writer](r: Result) = upickle.write(r)
    }
  }
}
