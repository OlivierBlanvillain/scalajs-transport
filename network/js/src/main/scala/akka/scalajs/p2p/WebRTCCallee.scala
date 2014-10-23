package akka.scalajs.p2p

import scala.scalajs.js

import akka.actor._
import akka.scalajs.common._
import akka.scalajs.jsapi._

object WebRTCCallee {
  def answerWithActor(handlerProps: ActorRef => Props)(implicit system: ActorSystem): ActorRef =
    system.actorOf(Props(new WebRTCCalleeProxy(handlerProps)))
}

private class WebRTCCalleeProxy(handlerProps: ActorRef => Props)
    extends WebRTCPeerProxy(handlerProps) {
  var peer: ActorRef = _
  
  override def receivedSignalingChannel(signalingChannel: ActorRef): Unit = {
    peer = signalingChannel
    peerConnection.ondatachannel = { event: Event =>
      setDataChannel(event.asInstanceOf[RTCDataChannelEvent].channel) // WebRTC API typo?
    }
  }

  override def receivedSessionDescription(remoteDescription: RTCSessionDescription): Unit = {
    peerConnection.setRemoteDescription(remoteDescription)
    peerConnection.createAnswer { localDescription: RTCSessionDescription =>
      peerConnection.setLocalDescription(localDescription)
      peer ! SessionDescription(js.JSON.stringify(localDescription))
    }  
  }
}
