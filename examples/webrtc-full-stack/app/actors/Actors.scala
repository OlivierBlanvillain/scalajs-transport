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
  RegisterPicklers.registerPicklers()
  
  var pendingPeer: Option[ActorRef] = None
  
  def receive = LoggingReceive {
    case m @ NewConnection() =>
      context.actorOf(Props(classOf[UserActor])) forward m

    case peer: ActorRef =>
      pendingPeer match {
        case None =>
          play.api.Logger.error("First!")
          pendingPeer = Some(peer)
        case Some(otherPeer) =>
          play.api.Logger.error("Let's party!")
      }
  }
}

class UserActor() extends Actor with ActorLogging {
  def receive = LoggingReceive {
    case m @ NewConnection() =>
      sender ! ActorWebSocket.actorForWebSocketHandler(self)
    case s: String =>
      // out ! ("I =?received your message: " + s)
      play.api.Logger.error("Got " + s)
  }
}
