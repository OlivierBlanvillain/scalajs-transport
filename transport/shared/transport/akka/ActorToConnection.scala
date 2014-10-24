package transport.p2p

import scala.concurrent._

import akka.actor._

import transport._

// TODO: private
class ActorToConnection(implicit ec: ExecutionContext) extends Actor {
  val peerPromise = Promise[ActorRef]()
  val messageListenerPromise = Promise[MessageListener]()
  
  override def receive = {
    case peer: ActorRef =>
      context.watch(peer)
      peerPromise.success(peer)
    case m: String =>
      messageListenerPromise.future.foreach(_.notify(m))
    case ActorToConnection.GetConnection =>
      val s = sender
      futureConnection.foreach(s ! _)
    case Terminated(_) =>
      context.stop(self)
  }
  
  override def postStop(): Unit = {
    messageListenerPromise.future.foreach(_.closed())
  }

  val futureConnection: Future[ConnectionHandle] = peerPromise.future.map { peer =>
    new ConnectionHandle {
      override def handlerPromise: Promise[MessageListener] =
        messageListenerPromise
      
      override def write(outboundPayload: String): Unit =
        peer ! outboundPayload
      
      override def close(): Unit =
        context.stop(self)
    }
  }
}
object ActorToConnection {
  def props(implicit ec: ExecutionContext) = Props(new ActorToConnection)
  case object GetConnection
}
