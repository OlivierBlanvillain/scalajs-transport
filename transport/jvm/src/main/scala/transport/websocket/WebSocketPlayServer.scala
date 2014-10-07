package transport.websocket

import scala.concurrent._
import scala.util.Success

import play.api.Application
import play.api.mvc._
import akka.actor._

import transport._

case class WebSocketPlayServer(socketRoute: Call)(implicit ec: ExecutionContext, app: Application)
    extends WebSocketTransport {
  private val promise = Promise[ConnectionListener]()
  
  /** */
  def action(): WebSocket[String, String] = WebSocket.tryAcceptWithActor[String, String] {
    request => Future.successful(
      promise.future.value match {
        case Some(Success(listener)) =>
          Right(BridgeActor.props(listener))
        case _ =>
          Left(???) // TODO Application.Forbidden
      }
    )
  }
  
  override def listen(): Future[(WebSocketUrl, Promise[ConnectionListener])] =
    Future.successful((WebSocketUrl(socketRoute.url), promise))
  
  override def connect(remote: WebSocketUrl): Future[ConnectionHandle] =
    Future.failed(new UnsupportedOperationException(
      "Servers cannot initiate WebSockets connections."))
  
  override def shutdown(): Future[Unit] =
    Future.successful(())
}

private class BridgeActor(listener: ConnectionListener, out: ActorRef)(
      implicit ec: ExecutionContext) extends Actor {
  private val promise = Promise[MessageListener]()
  private var poorMansBuffer: Future[MessageListener] = promise.future

  private val connectionHandle = new ConnectionHandle {
    override def handlerPromise: Promise[MessageListener] = promise
    override def write(outboundPayload: String): Unit = out ! outboundPayload
    override def close(): Unit = context.stop(self)
  }
  
  override def preStart: Unit = {
    listener.notify(connectionHandle)
  }
  
  override def postStop: Unit = {
    poorMansBuffer = poorMansBuffer.andThen {
      case Success(l) => l.closed()
    }
  }
  
  override def receive = {
    case inboundPayload: String =>
      poorMansBuffer = poorMansBuffer.andThen {
        case Success(l) => l.notify(inboundPayload)
      }
  }
}
private object BridgeActor {
  def props(listener: ConnectionListener)(out: ActorRef)(implicit ec: ExecutionContext) =
    Props(new BridgeActor(listener, out))
}
