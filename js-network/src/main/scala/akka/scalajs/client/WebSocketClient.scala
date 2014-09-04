package akka.scalajs.client

import akka.actor._

import scala.scalajs.js
import akka.scalajs.jsapi._

import org.scalajs.spickling._
import org.scalajs.spickling.jsany._

case class WebSocketClient(url: String)(implicit system: ActorSystem) {
  def connectWithActor(handlerProps: ActorRef => Props): ActorRef =
    system.actorOf(Props(new WebSocketClientProxy(url, handlerProps)))
}
private class WebSocketClientProxy(url: String, handlerProps: ActorRef => Props) extends Actor {
  var handlerActor: ActorRef = _
  var webSocket: WebSocket = _

  override def preStart() = {
    handlerActor = context.watch(context.actorOf(handlerProps(self)))
    
    webSocket = new WebSocket(url)
    webSocket.addEventListener("message", { (event: Event) =>
      val pickle = js.JSON.parse(event.asInstanceOf[MessageEvent].data.toString())
      val message = PicklerRegistry.unpickle(pickle.asInstanceOf[js.Any])
      handlerActor ! message
    }, useCapture = false)
    webSocket.addEventListener("close", { (event: Event) =>
      handlerActor ! PoisonPill
    }, useCapture = false)
    webSocket.addEventListener("error", { (event: Event) =>
      handlerActor ! PoisonPill
    }, useCapture = false)
  }

  override def postStop() = {
    webSocket.close()
  }

  def receive = {
    case Terminated(a) if a == handlerActor =>
      context.stop(self)
    case message =>
      val pickle = PicklerRegistry.pickle(message)
      webSocket.send(js.JSON.stringify(pickle))
  }
}
