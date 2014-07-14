package akka.scalajs.wsclient

import akka.actor._
import akka.scalajs.wscommon._

import scala.scalajs.js
import akka.scalajs.jsapi._

import org.scalajs.spickling._
import org.scalajs.spickling.jsany._

object ClientProxy {
  case object ConnectionError
}

case class WebSocketConnected(entryPointRef: ActorRef)

class ClientProxy(wsUrl: String, connectedHandler: ActorRef) extends AbstractProxy {
  /** Will send the WebSocketConnected message to parent actor. */
  def this(wsUrl: String) = this(wsUrl, null)

  import AbstractProxy._
  import ClientProxy._

  type PickleType = js.Any
  implicit protected def pickleBuilder: PBuilder[PickleType] = JSPBuilder
  implicit protected def pickleReader: PReader[PickleType] = JSPReader

  var sockjs: SockJS = _

  override def preStart() = {
    super.preStart()

    sockjs = new SockJS(wsUrl, null, js.Dictionary(
      "debug" -> true,
      "protocols_whitelist" -> js.Array("xdr-streaming", "xhr-streaming", "iframe-eventsource", "iframe-htmlfile", "xdr-polling", "xhr-polling", "iframe-xhr-polling", "jsonp-polling")
    ))
    sockjs.addEventListener("message", { (event: Event) =>
      self ! IncomingMessage(js.JSON.parse(
          event.asInstanceOf[MessageEvent].data.toString()))
    }, useCapture = false)
    sockjs.addEventListener("close", { (event: Event) =>
      self ! ConnectionClosed
    }, useCapture = false)
    sockjs.addEventListener("error", { (event: Event) =>
      self ! ConnectionError
    }, useCapture = false)
  }

  override def postStop() = {
    super.postStop()
    sockjs.close()
  }

  override def receive = super.receive.orElse[Any, Unit] {
    case ConnectionError =>
      throw new akka.AkkaException("sockjs connection error")
  }

  override def receiveFromPeer = super.receiveFromPeer.orElse[Any, Unit] {
    case Welcome(entryPointRef) =>
      val msg = WebSocketConnected(entryPointRef)
      if (connectedHandler eq null) context.parent ! msg
      else connectedHandler ! msg
  }

  override protected def sendPickleToPeer(pickle: PickleType): Unit = {
    sockjs.send(js.JSON.stringify(pickle))
  }

}
