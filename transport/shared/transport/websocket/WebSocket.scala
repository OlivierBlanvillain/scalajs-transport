package transport.websocket

import transport._
import scala.concurrent._

case class WebSocketUrl(url: String)

trait WebSocketTransport extends Transport {
  type Address = WebSocketUrl
}

class WebSocketServer extends WebSocketTransport {
  override def listen(): Future[(WebSocketUrl, Promise[Transport.ConnectionListener])] =
    ???
  
  override def connect(remote: WebSocketUrl): Future[ConnectionHandle] =
    Future.failed(new UnsupportedOperationException(""))
  
  override def shutdown(): Future[Unit] =
    Future.successful(())
}

trait SockJSTransport extends Transport
trait SockJSClient extends SockJSTransport
trait SockJSServer extends SockJSTransport
trait WebRTCTransport extends Transport
