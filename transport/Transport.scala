import scala.concurrent._

case class TransportException(message: String) extends Exception(message)

trait PickleType

trait Transport {
  type Address
  
  def listen(): Future[(Address, Promise[Transport.ConnectionListener])]
  def connect(remoteAddress: Address): Future[ConnectionHandle]
  def shutdown(): Future[Unit]
}
object Transport {
  trait ConnectionListener {
    def notify(inboundConnection: ConnectionHandle): Unit
  }
}

trait ConnectionHandle {
  def handlerPromise(): Promise[ConnectionHandle.MessageListener]
  def write(outboundPayload: PickleType): Unit
  def disconnect(): Unit
}
object ConnectionHandle {
  trait MessageListener {
    def notify(inboundPayload: PickleType): Unit
    def disconnected(): Unit
  }
}

trait SockJSTransport extends Transport
trait SockJSClient extends SockJSTransport
trait SockJSServer extends SockJSTransport
trait WebRTCTransport extends Transport

trait WebSocketTransport extends Transport {
  case class WebSocketUrl(url: String)
  
  type Address = WebSocketUrl
}

class WebSocketClient extends WebSocketTransport {
  override def listen(): Future[(WebSocketUrl, Promise[Transport.ConnectionListener])] =
    Future.failed(TransportException(""))
  
  override def connect(remoteAddress: WebSocketUrl): Future[ConnectionHandle] =
    ???
  
  override def shutdown(): Future[Unit] =
    Future.successful()
}

class WebSocketServer extends WebSocketTransport {
  override def listen(): Future[(WebSocketUrl, Promise[Transport.ConnectionListener])] =
    ???
  
  override def connect(remoteAddress: WebSocketUrl): Future[ConnectionHandle] =
    Future.failed(TransportException(""))
  
  override def shutdown(): Future[Unit] =
    Future.successful()
}
