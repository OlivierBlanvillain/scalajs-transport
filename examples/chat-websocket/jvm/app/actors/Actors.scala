package actors

import akka.actor._

import models._

class PeerMatcher extends Actor {
  def receive: Receive = {
    case NewConnection(user) =>
      context.watch(user)
      context.become(pending(user))
  }
    
  def pending(user: ActorRef): Receive = {
    case NewConnection(otherUser) =>
      context.unwatch(user)
      user ! Connected(otherUser)
      otherUser ! Connected(user)
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

  def receive = Actor.emptyBehavior
}
object UserActor {
  def props(board: ActorRef)(out: ActorRef) = Props(new UserActor(out, board))
}

case class NewConnection(remote: ActorRef)
