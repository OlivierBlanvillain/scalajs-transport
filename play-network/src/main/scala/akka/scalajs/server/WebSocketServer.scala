package akka.scalajs.server

import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current

import akka.actor._

import org.scalajs.spickling._
import org.scalajs.spickling.playjson._

object WebSocketServer {
  def acceptWithActor(handlerProps: ActorRef => Props): WebSocket[JsValue, JsValue] = {
    WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
      Props(new WebSocketServerProxy(out, handlerProps))
    }
  }
}

private class WebSocketServerProxy(out: ActorRef, handlerProps: ActorRef => Props) extends Actor {
  var handlerActor: ActorRef = _
  
  override def preStart() = {
    val pickleAndForward = context.watch(context.actorOf(Props(new PickleAndForward(out))))
    handlerActor = context.watch(context.actorOf(handlerProps(pickleAndForward)))
  }

  def receive = {
    case Terminated(a) if a == handlerActor =>
      context.stop(self)
    case pickle =>
      handlerActor ! PicklerRegistry.unpickle(pickle.asInstanceOf[JsValue])
  }
}

private class PickleAndForward(out: ActorRef) extends Actor {
  def receive = {
    case message =>
      out ! PicklerRegistry.pickle(message)
  }
}
  
