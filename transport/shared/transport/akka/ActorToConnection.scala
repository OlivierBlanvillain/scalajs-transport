package transport.p2p

import scala.concurrent._
import scala.util.{ Success, Failure }

import akka.actor._

import transport._

private class ActorToConnection(connectionPromise: Promise[ConnectionHandle])(
      implicit ec: ExecutionContext) extends Actor {
  val promise = QueueablePromise[MessageListener]()
  val peerPromise = Promise[ActorRef]()
  
  def receive = {
    case peer: ActorRef =>
      context.watch(peer)
      peerPromise.success(peer)
    case Terminated(_) =>
      context.stop(self)
    case m: String =>
      promise.queue(_.notify(m))
  }
  
  override def postStop(): Unit = {
    promise.queue(_.closed())
  }
  
  peerPromise.future.map { peer =>
    connectionPromise.success(
      new ConnectionHandle {
        def handlerPromise: Promise[MessageListener] = promise
        def write(outboundPayload: String): Unit = peer ! outboundPayload
        def close(): Unit = context.stop(self)
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
