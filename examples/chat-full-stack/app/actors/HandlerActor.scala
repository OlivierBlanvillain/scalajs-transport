package actors

import akka.actor._
import models.ConnectionEstablished

class HandlerActor(chatManager: ActorRef, out: ActorRef) extends Actor {
  override def preStart() = {
    chatManager ! NewConnection(self, out)
  }
  
  override def receive = {
    case userManager: ActorRef =>
      out ! ConnectionEstablished(userManager)
  }
}
object HandlerActor {
  def props(chatManager: ActorRef, out: ActorRef) = Props(new HandlerActor(chatManager, out))
}
