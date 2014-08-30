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

class BoardActor extends Actor with ActorLogging {
  var users = Set[ActorRef]()

  def receive = LoggingReceive {
    case Subscribe =>
      users += sender
      context watch sender
    case Terminated(user) =>
      users -= user
    case m =>
      users foreach { _ ! m }
  }
}

class UserActor(out: ActorRef, board: ActorRef) extends Actor with ActorLogging {
  override def preStart() = {
    board ! Subscribe
  }
  def receive = LoggingReceive {
    case New(m) =>
      out ! m
    case m =>
      board ! New(m)
      play.api.Logger.error("Got " + m.toString)
  }
}
object UserActor {
  def props(board: ActorRef, out: ActorRef) = Props(new UserActor(out, board))
}

object Subscribe
case class New(m: Any)
