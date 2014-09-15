package akka.scalajs.server

import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current

import akka.actor._
import akka.scalajs.wscommon.AbstractProxy

import org.scalajs.spickling._
import org.scalajs.spickling.playjson._

object WebSocketServer {
  def acceptWithActor(handlerProps: ActorRef => Props): WebSocket[JsValue, JsValue] = { 
    WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
      Props(new WebSocketServerProxy(out, handlerProps))
    }
  }
}

private class WebSocketServerProxy(out: ActorRef, handlerProps: ActorRef => Props)
    extends AbstractProxy(handlerProps) {
  import AbstractProxy._
  
  type PickleType = JsValue
  implicit protected def pickleBuilder: PBuilder[PickleType] = PlayJsonPBuilder
  implicit protected def pickleReader: PReader[PickleType] = PlayJsonPReader

  override def preStart(): Unit = {
    super.preStart()
    self ! ConnectionOpened
  }

  override protected def sendPickleToPeer(pickle: PickleType): Unit = {
    out ! pickle
  }
}
