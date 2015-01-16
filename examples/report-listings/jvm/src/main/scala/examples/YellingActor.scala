package examples

import akka.actor._
import transport.akka
import transport._

class YellingActor(out: ActorRef) extends Actor {
  override def preStart = println("Connected")
  override def postStop = println("Disconnected")
  def receive = {
    case message: String =>
      println("Received: " + message)
      out ! message.toUpperCase
  }
}
