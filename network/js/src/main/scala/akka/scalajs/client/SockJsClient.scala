package akka.scalajs.client

import scala.scalajs.js

import akka.actor._
import akka.scalajs.jsapi._
import akka.scalajs.common.AbstractProxy

import org.scalajs.spickling._
import org.scalajs.spickling.jsany._

case class SockJSClient(url: String)(implicit system: ActorSystem) {
  def connectWithActor(handlerProps: ActorRef => Props): ActorRef = {
    system.actorOf(Props(new SockJSClientProxy(url, handlerProps)))
  }
}
private class SockJSClientProxy(url: String, handlerProps: ActorRef => Props)
    extends AbstractProxy(handlerProps) {
  import AbstractProxy._
  
  type PickleType = js.Any
  implicit protected def pickleBuilder: PBuilder[PickleType] = JSPBuilder
  implicit protected def pickleReader: PReader[PickleType] = JSPReader

  var sockjs: SockJS = _

  override def preStart(): Unit = {
    super.preStart()
    self ! ConnectionOpened // TODO: Better detect connection establishment
    sockjs = new SockJS(url)
    sockjs.addEventListener("message", { (event: Event) =>
      val pickle = js.JSON.parse(event.asInstanceOf[MessageEvent].data.toString())
      self ! pickle
    }, useCapture = false)
    sockjs.addEventListener("close", { (event: Event) =>
      self ! ConnectionClosed
    }, useCapture = false)
    sockjs.addEventListener("error", { (event: Event) =>
      self ! ConnectionClosed
    }, useCapture = false)
  }

  override def postStop() = {
    super.postStop()
    sockjs.close()
  }

  override protected def sendPickleToPeer(pickle: PickleType): Unit = {
    sockjs.send(js.JSON.stringify(pickle))
  }
}
