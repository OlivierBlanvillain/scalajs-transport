package transport.p2p

import scala.concurrent._
import scala.util.{ Success, Failure }
import scala.scalajs.js

import akka.actor._

import transport._
import transport.jsapi._

class WebRTCTransport(implicit ec: ExecutionContext) extends Transport {
  type Address = ConnectionHandle
  
  def listen(): Future[Promise[ConnectionListener]] = 
    Future.failed(new UnsupportedOperationException("TODO"))

  def connect(signalingChannel: ConnectionHandle): Future[ConnectionHandle] = {
    new WebRTCCaller(signalingChannel).futureConnection
  }

  def shutdown(): Unit = ()
}

class WebRTCCallee() extends WebRTCPeer

class WebRTCCaller(signalingChannel: ConnectionHandle) extends WebRTCPeer

abstract class WebRTCPeer() {
  def futureConnection: Future[ConnectionHandle] = ???
}

 // case class IceCandidate(string: String)
// case class SessionDescription(string: String)
// case class SignalingChannel(peer: ActorRef)

// import org.scalajs.spickling._
// import org.scalajs.spickling.jsany._

// object RegisterWebRTCPicklers {
//   import PicklerRegistry.register

//   register[IceCandidate]
//   register[SessionDescription]
//   register[SignalingChannel]

//   def registerPicklers(): Unit = ()
// }


// private class WebRTCCalleeProxy(handlerProps: ActorRef => Props)
//     extends WebRTCPeer(handlerProps) {
//   var peer: ActorRef = _
  
//   override def receivedSignalingChannel(signalingChannel: ActorRef): Unit = {
//     peer = signalingChannel
//     peerConnection.ondatachannel = { event: Event =>
//       setDataChannel(event.asInstanceOf[RTCDataChannelEvent].channel) // WebRTC API typo?
//     }
//   }

//   override def receivedSessionDescription(remoteDescription: RTCSessionDescription): Unit = {
//     peerConnection.setRemoteDescription(remoteDescription)
//     peerConnection.createAnswer { localDescription: RTCSessionDescription =>
//       peerConnection.setLocalDescription(localDescription)
//       peer ! SessionDescription(js.JSON.stringify(localDescription))
//     }  
//   }
// }


// private class WebRTCCallerProxy(calleeRef: ActorRef, handlerProps: ActorRef => Props)
//     extends WebRTCPeer(handlerProps) {
//   override def preStart(): Unit = {
//     super.preStart()
//     self ! SignalingChannel(calleeRef)
//     calleeRef ! SignalingChannel(self)
//   }
  
//   override def receivedSignalingChannel(peer: ActorRef): Unit = {
//     setDataChannel(peerConnection.createDataChannel("sendDataChannel"))
//     peerConnection.createOffer { description: RTCSessionDescription =>
//       peerConnection.setLocalDescription(description)
//       peer ! SessionDescription(js.JSON.stringify(description))
//     }
//   }
  
//   override def receivedSessionDescription(description: RTCSessionDescription): Unit = {
//     peerConnection.setRemoteDescription(description)
//   }
// }


// private abstract class WebRTCPeer(handlerProps: ActorRef => Props) extends Actor {
//   RegisterWebRTCPicklers.registerPicklers()
//   var peerConnection: webkitRTCPeerConnection = _
//   var dataChannel: Option[RTCDataChannel] = None
//   var handlerActor: ActorRef = _

//   override def preStart(): Unit = {
//     peerConnection = new webkitRTCPeerConnection(null, DataChannelsConstraint)
//   }
  
//   override def postStop(): Unit = {
//     dataChannel.foreach(_.close())
//     peerConnection.close()
//   }

//   override def receive = {
//     case SignalingChannel(peer: ActorRef) =>
//       peerConnection.onicecandidate = { event: RTCIceCandidateEvent =>
//         if(event.candidate != null) {
//           peer ! IceCandidate(js.JSON.stringify(event.candidate))
//         }
//       }
//       receivedSignalingChannel(peer)
//     case SessionDescription(description) =>
//       receivedSessionDescription(
//         new RTCSessionDescription(js.JSON.parse(description).asInstanceOf[RTCSessionDescriptionInit])
//       )
//     case IceCandidate(candidate) =>
//       peerConnection.addIceCandidate(new RTCIceCandidate(js.JSON.parse(candidate).asInstanceOf[RTCIceCandidate]))
//     case Terminated(a) if a == handlerActor =>
//       context.stop(self)
//     case message =>
//       val pickle = PicklerRegistry.pickle(message)
//       dataChannel.foreach(_.send(js.JSON.stringify(pickle)))
//   }
  
//   def setDataChannel(dc: RTCDataChannel) = {
//     dc.onopen = { event: Event =>
//       handlerActor = context.watch(context.actorOf(handlerProps(self)))
//     }
//     dc.onmessage = { event: RTCMessageEvent =>
//       val pickle = js.JSON.parse(event.data.toString())
//       val message = PicklerRegistry.unpickle(pickle.asInstanceOf[js.Any])
//       handlerActor ! message
//     }
//     dc.onclose = { event: Event =>
//       handlerActor ! PoisonPill
//     }
//     dc.onerror = { event: Event =>
//       handlerActor ! PoisonPill
//     }
//     dataChannel = Some(dc)
//   }

//   def receivedSignalingChannel(peer: ActorRef): Unit

//   def receivedSessionDescription(description: RTCSessionDescription): Unit
// }

private object OptionalMediaConstraint extends RTCOptionalMediaConstraint {
  override val DtlsSrtpKeyAgreement: js.Boolean = false
  override val RtpDataChannels: js.Boolean = true
}
private object DataChannelsConstraint extends RTCMediaConstraints {
  override val mandatory: RTCMediaOfferConstraints = null
  override val optional: js.Array[RTCOptionalMediaConstraint] = js.Array(OptionalMediaConstraint)
}

