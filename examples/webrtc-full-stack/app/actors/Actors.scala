package actors

import scala.language.postfixOps

import scala.concurrent.duration._

import akka.actor._
import akka.actor.SupervisorStrategy.{Stop, Escalate}
import akka.event.LoggingReceive
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import akka.scalajs.wsserver._
import akka.scalajs.wscommon.AbstractProxy.Welcome

import models._

class PeerMatcher extends Actor with ActorLogging {
  def receive: Receive = {
    case NewConnection =>
      context.watch(sender)
      context.become(pending(sender))
  }
    
  def pending(user: ActorRef): Receive = {
    case NewConnection =>
      context.unwatch(sender)
      sender ! Connected(user)
      user ! Connected(sender)
      context.unbecome()
    case Terminated(_) =>
      context.unbecome()
  }
}
object PeerMatcher {
  val props = Props(new PeerMatcher())
}

class UserActor(out: ActorRef, board: ActorRef) extends Actor with ActorLogging {
  override def preStart() = {
    board ! NewConnection
  }

  def receive: Receive = {
    case Connected(peer) =>
      out ! PeerFound
      context.watch(peer)
      context.become(connected(peer))
  }
  
  def connected(peer: ActorRef): Receive = {
    case Forward(m) =>
      out ! m
    case m @ Terminated(_) =>
      context.stop(self)
    case m =>
      peer ! Forward(m)
  }
}
object UserActor {
  def props(board: ActorRef, out: ActorRef) = Props(new UserActor(out, board))
}

case class Connected(peer: ActorRef)
case class Forward(message: Any)
object NewConnection
