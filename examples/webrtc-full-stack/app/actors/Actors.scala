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
    case m: Message =>
      users foreach { _ ! m }
    case Subscribe =>
      users += sender
      context watch sender
    case Terminated(user) =>
      users -= user
  }
}

class UserActor(out: ActorRef, board: ActorRef) extends Actor with ActorLogging {
  override def preStart() = {
    board ! Subscribe
  }
  def receive = LoggingReceive {
    case Message(s) if sender == board =>
      out ! s
    case s: String =>
      board ! Message(s)
      play.api.Logger.error("Got " + s)
    case m =>
      play.api.Logger.error("What is that? " + m.toString)
  }
}
object UserActor {
  def props(board: ActorRef)(out: ActorRef) = Props(new UserActor(out, board))
}

// class PeerMatcher extends Actor with ActorLogging {
//   RegisterPicklers.registerPicklers()
  
//   var pendingPeer: Option[ActorRef] = None
  
//   def receive = LoggingReceive {
//     case m @ NewConnection =>
//       context.actorOf(Props(classOf[UserActor])) forward m

//     case peer: ActorRef =>
//       pendingPeer match {
//         case None =>
//           play.api.Logger.error("First!")
//           pendingPeer = Some(peer)
//         case Some(otherPeer) =>
//           play.api.Logger.error("Let's party!")
//       }
//   }
// }

// class UserActor(out: ActorRef) extends Actor with ActorLogging {
//   def receive = LoggingReceive {
//     case m @ NewConnection =>
//       play.api.Logger.error("NewConnection!")
//       // sender ! ActorWebSocket.actorForWebSocketHandler(self)
//     case s: String =>
//       out ! ("I received your message: " + s)
//       play.api.Logger.error("Got " + s)
//   }
// }
