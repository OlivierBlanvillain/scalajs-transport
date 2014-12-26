package transport.tyrus

import transport._
import scala.concurrent._
import scala.util._

import java.net.URI
import javax.websocket._
import org.glassfish.tyrus.client.ClientManager

/** WebSocket client for the JVM based on the [[https://tyrus.java.net/ Tyrus project]]. */
class WebSocketClient(implicit ec: ExecutionContext) extends WebSocketTransport {
  def listen(): Future[Promise[ConnectionListener]] =
    Future.failed(new UnsupportedOperationException(
      "WebSocketClient cannot listen for incomming connections."))
  
  def connect(remote: WebSocketUrl): Future[ConnectionHandle] = {
    val (endPoint, futureConnection) = EndpointToConnection()
  
    val cec = ClientEndpointConfig.Builder.create().build()
    ClientManager.createClient().connectToServer(endPoint, cec, new URI(remote.url))
  
    futureConnection
  }
  
  def shutdown(): Future[Unit] = Future.successful(Unit)
}
