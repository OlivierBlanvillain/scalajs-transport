package akka.scalajs.server

import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current
import play.sockjs.api._

import akka.actor._
import akka.scalajs.common.AbstractProxy

import org.scalajs.spickling._
import org.scalajs.spickling.playjson._

object SockJSServer {
  def acceptWithActor(handlerProps: ActorRef => Props): SockJSRouter = { 
    SockJSRouter.acceptWithActor[JsValue, JsValue] { request => out =>
      Props(new SockJSServerProxy(out, handlerProps))
    }
  }
}

private class SockJSServerProxy(out: ActorRef, handlerProps: ActorRef => Props)
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
