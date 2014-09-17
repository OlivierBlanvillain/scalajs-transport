package actors

import akka.actor._

case class NewConnection(handler: ActorRef, remote: ActorRef)
