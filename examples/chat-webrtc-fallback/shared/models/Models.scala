package models

import akka.actor.ActorRef

case class Msg(text: String)
case class Connected(peer: ActorRef)
