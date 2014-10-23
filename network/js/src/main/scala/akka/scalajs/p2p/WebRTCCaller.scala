package akka.scalajs.p2p

import scala.scalajs.js

import akka.actor._
import akka.scalajs.common._
import akka.scalajs.jsapi._

case class WebRTCCaller(calleeRef: ActorRef) {
  def callWithActor(handlerProps: ActorRef => Props)(implicit system: ActorSystem): ActorRef =
    system.actorOf(Props(new WebRTCCallerProxy(calleeRef, handlerProps)))
}

private class WebRTCCallerProxy(calleeRef: ActorRef, handlerProps: ActorRef => Props)
    extends WebRTCPeerProxy(handlerProps) {
  override def preStart(): Unit = {
    super.preStart()
    self ! SignalingChannel(calleeRef)
    calleeRef ! SignalingChannel(self)
  }
  
  override def receivedSignalingChannel(peer: ActorRef): Unit = {
    setDataChannel(peerConnection.createDataChannel("sendDataChannel"))
    peerConnection.createOffer { description: RTCSessionDescription =>
      peerConnection.setLocalDescription(description)
      peer ! SessionDescription(js.JSON.stringify(description))
    }
  }
  
  override def receivedSessionDescription(description: RTCSessionDescription): Unit = {
    peerConnection.setRemoteDescription(description)
  }
}
