package transport.p2p

import scala.concurrent._
import scala.util.{ Success, Failure }

import akka.actor._

import transport._

private class ActorToConnection(connectionPromise: Promise[ConnectionHandle])(
      implicit ec: ExecutionContext) extends Actor {
  val messageListenerPromise = Promise[MessageListener]()
  var poorMansBuffer: Future[MessageListener] = messageListenerPromise.future
  val peerPromise = Promise[ActorRef]()
  
  def receive = {
    case peer: ActorRef =>
      context.watch(peer)
      peerPromise.success(peer)
    case Terminated(_) =>
      context.stop(self)
    case m: String =>
      poorMansBuffer = poorMansBuffer.andThen {
        case Success(listener) =>
          listener.notify(m)
      }
  }
  
  override def postStop(): Unit = {
    poorMansBuffer = poorMansBuffer.andThen {
      case Success(listener) =>
        listener.closed()
    }
  }
  
  peerPromise.future.map { peer =>
    connectionPromise.success(
      new ConnectionHandle {
        def handlerPromise: Promise[MessageListener] =
          messageListenerPromise
        
        def write(outboundPayload: String): Unit =
          peer ! outboundPayload
        
        def close(): Unit =
          context.stop(self)
      }
    )
  }
}
object ActorToConnection {
  def apply(system: ActorSystem)(implicit ec: ExecutionContext):
      (ActorRef, Future[ConnectionHandle]) = {
    val promise = Promise[ConnectionHandle]()
    val ref = system.actorOf(Props(new ActorToConnection(promise)))
    (ref, promise.future)
  }
}
