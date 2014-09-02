package akka.scalajs.p2p

import akka.actor._
import akka.scalajs.jsapi._

class CallerProxy(connectedHandler: ActorRef) extends PeerProxy(connectedHandler) {
  
  import PeerProxy._
  
  override def receivedSignalingChannel(peer: ActorRef): Unit = {
    dataChannel = Some(peerConnection.createDataChannel("sendDataChannel"))
    println("Created send data channel")
    
    dataChannel foreach setDataChannelCallbacks
    
    peerConnection.createOffer(
      { (description: RTCSessionDescription) =>
        peerConnection.setLocalDescription(description)
        println("Offer from localPeerConnection")
        println(description.sdp)
        peer ! SessionDescription(description)
      }
    )
  }
  
  override def receivedSessionDescription(description: RTCSessionDescription): Unit = {
    peerConnection.setRemoteDescription(description)
  }

}
