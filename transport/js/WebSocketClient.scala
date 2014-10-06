package transport

import scala.concurrent._

class WebSocketClient extends WebSocketTransport {
  override def listen(): Future[(WebSocketUrl, Promise[Transport.ConnectionListener])] =
    Future.failed(new UnsupportedOperationException(""))
  
  override def connect(remote: WebSocketUrl): Future[ConnectionHandle] =
    ???
  
  override def shutdown(): Future[Unit] =
    Future.successful(())
}
