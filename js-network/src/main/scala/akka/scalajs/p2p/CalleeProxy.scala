package akka.scalajs.p2p

import akka.actor._
import akka.scalajs.jsapi._

class CalleeProxy(connectedHandler: ActorRef) extends PeerProxy(connectedHandler) {

  import PeerProxy._
  
  var signalingChannel: ActorRef = _
  
  override def receivedSignalingChannel(peer: ActorRef): Unit = {
    signalingChannel = peer
    peerConnection.ondatachannel = { (event: Event) =>
      println("Receive Channel Callback")
      this.dataChannel = Some(event.asInstanceOf[RTCDataChannelEvent].channel)
      dataChannel = Some(event.asInstanceOf[RTCDataChannelEvent].channel) // WebRTC API typo?
      dataChannel foreach setDataChannelCallbacks
      ()
    }
  }

  override def receivedSessionDescription(description: RTCSessionDescription): Unit = {
    peerConnection.setRemoteDescription(description)
    peerConnection.createAnswer(
      { (description: RTCSessionDescription) =>
        peerConnection.setLocalDescription(description)
        signalingChannel ! SessionDescription(description)
        println("Answer from remotePeerConnection")
        println(description.sdp)
      }  
    )
  }
  
}
