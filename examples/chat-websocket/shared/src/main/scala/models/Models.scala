package models

import akka.actor.ActorRef

case class Connected(peer: ActorRef)
case class Msg(text: String)
