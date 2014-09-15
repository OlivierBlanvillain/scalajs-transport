package actors

import akka.actor._
// import akka.scalajs.p2p.RegisterWebRTCPicklers

import models._

class PeerMatcher extends Actor {
  // RegisterWebRTCPicklers.registerPicklers()
  
  override def receive: Receive = {
    case NewConnection(user) =>
      context.watch(user)
      context.become(pending(user))
  }
    
  def pending(user: ActorRef): Receive = {
    case NewConnection(otherUser) =>
      context.unwatch(user)
      user ! Connected(otherUser)
      context.unbecome()
    
    case Terminated(ref) if ref == user =>
      context.unbecome()
  }
}
object PeerMatcher {
  val props = Props(new PeerMatcher())
}

class UserActor(out: ActorRef, board: ActorRef) extends Actor {
  override def preStart(): Unit = {
    board ! NewConnection(out)
  }

  override def receive = Actor.emptyBehavior
}
object UserActor {
  def props(board: ActorRef, out: ActorRef) = Props(new UserActor(out, board))
}

case class NewConnection(remote: ActorRef)
