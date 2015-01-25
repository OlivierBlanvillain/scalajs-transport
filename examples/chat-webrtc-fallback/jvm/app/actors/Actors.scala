package actors

import akka.actor._
import transport.{ ConnectionUtils, ConnectionHandle }

import play.api.libs.concurrent.Execution.Implicits._

class PeerMatcher extends Actor {
  
  def receive: Receive = {
    case NewConnection(user, connection) =>
      context.watch(user)
      context.become(pending(user, connection))
  }
    
  def pending(user: ActorRef, connection: ConnectionHandle): Receive = {
    case NewConnection(otherUser, otherConnection) =>
      context.unwatch(user)
      context.unbecome()
      ConnectionUtils.plug(connection, otherConnection)
      user ! "Plugged"
      otherUser ! "Plugged"
    
    case Terminated(ref) if ref == user =>
      context.unbecome()
  }
}
object PeerMatcher {
  val props = Props(new PeerMatcher())
}

class UserActor(out: ActorRef, connection: ConnectionHandle, board: ActorRef) extends Actor {
  override def preStart(): Unit = {
    board ! NewConnection(out, connection)
  }

  def receive = Actor.emptyBehavior
}
object UserActor {
  def props(board: ActorRef, connection: ConnectionHandle)(out: ActorRef) =
    Props(new UserActor(out, connection,  board))
}

case class NewConnection(remote: ActorRef, connection: ConnectionHandle)
