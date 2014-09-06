package akka.scalajs.client

import scala.scalajs.js

import akka.actor._
import akka.scalajs.jsapi._
import akka.scalajs.wscommon.AbstractProxy

import org.scalajs.spickling._
import org.scalajs.spickling.jsany._

case class WebSocketClient(url: String)(implicit system: ActorSystem) {
  def connectWithActor(handlerProps: ActorRef => Props): ActorRef = {
    system.actorOf(Props(new WebSocketClientProxy(url, handlerProps)))
  }
}
private class WebSocketClientProxy(url: String, handlerProps: ActorRef => Props) extends AbstractProxy(handlerProps) {
  import AbstractProxy._
  
  type PickleType = js.Any
  implicit protected def pickleBuilder: PBuilder[PickleType] = JSPBuilder
  implicit protected def pickleReader: PReader[PickleType] = JSPReader

  var webSocket: WebSocket = _

  override def preStart(): Unit = {
    super.preStart()
    self ! ConnectionOpened
    webSocket = new WebSocket(url)
    // webSocket.addEventListener("open", { (event: Event) =>
      // self ! ConnectionOpened
    // }, useCapture = false)
    webSocket.addEventListener("message", { (event: Event) =>
      val pickle = js.JSON.parse(event.asInstanceOf[MessageEvent].data.toString())
      self ! pickle
    }, useCapture = false)
    webSocket.addEventListener("close", { (event: Event) =>
      self ! ConnectionClosed
    }, useCapture = false)
    webSocket.addEventListener("error", { (event: Event) =>
      self ! ConnectionClosed
    }, useCapture = false)
  }

  override def postStop() = {
    super.postStop()
    webSocket.close()
  }

  override protected def sendPickleToPeer(pickle: PickleType): Unit = {
    webSocket.send(js.JSON.stringify(pickle))
  }
}
