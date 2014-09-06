package actors

import play.api.Logger

import akka.actor._

import models._

class PeerMatcher extends Actor with ActorLogging {
  override def postStop = {
    play.api.Logger.error("postStop!")
  }
  
  override def receive: Receive = {
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

class UserActor(out: ActorRef, board: ActorRef) extends Actor with ActorLogging {
  override def preStart(): Unit = {
    board ! NewConnection(out)
  }

  override def receive: Receive =  {
    case m => Logger.error(m.toString)
  }
}
object UserActor {
  def props(board: ActorRef, out: ActorRef) = Props(new UserActor(out, board))
}

case class NewConnection(remote: ActorRef)
