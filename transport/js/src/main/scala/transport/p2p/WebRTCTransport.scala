package transport.p2p

import scala.concurrent._
import scala.util.{ Success, Failure }
import scala.scalajs.js

import akka.actor._

import transport._
import transport.jsapi._

import org.scalajs.spickling._
import org.scalajs.spickling.jsany._

class WebRTCTransport(implicit ec: ExecutionContext) extends Transport {
  type Address = ConnectionHandle
  
  def listen(): Future[Promise[ConnectionListener]] = 
    Future.failed(new UnsupportedOperationException("TODO"))

  def connect(signalingChannel: ConnectionHandle): Future[ConnectionHandle] = {
    new WebRTCPeer(signalingChannel, js.Math.random()).future
  }

  def shutdown(): Unit = ()
}

private class WebRTCPeer(signalingChannel: ConnectionHandle, priority: Double)(
      implicit ec: ExecutionContext) {
  RegisterWebRTCPicklers.registerPicklers()
  
  private val webRTCConnection = new webkitRTCPeerConnection(null, DataChannelsConstraint)
  private val connectionPromise = Promise[ConnectionHandle]()
  private var isCaller: Boolean = _

  signalingChannel.handlerPromise.success(new MessageListener {
    def notify(inboundPayload: String) = {
      val parsedPayload : js.Any = js.JSON.parse(inboundPayload)
      val unpickledPayload: Any = PicklerRegistry.unpickle(parsedPayload)
      revievedViaSignaling(unpickledPayload)
    }
    override def closed() = if(!future.isCompleted) {
      connectionPromise.failure(new IllegalStateException(
        "Signaling channel closed before the end of connection establishment."))
    }
  })

  webRTCConnection.onicecandidate = { event: RTCIceCandidateEvent =>
    if(event.candidate != null) {
      sendViaSignaling(IceCandidate(js.JSON.stringify(event.candidate)))
    }
  }
  
  sendViaSignaling(Priority(priority))
  
  def future: Future[ConnectionHandle] = connectionPromise.future

  private def sendViaSignaling(m: Any): Unit = {
    signalingChannel.write(js.JSON.stringify(PicklerRegistry.pickle(m)))
  }

  private def revievedViaSignaling(m: Any): Unit = {
    // Each message is received exactly once, in the order of appearance in this match.
    m match {
      
      case Priority(hisPriority) =>
        isCaller = hisPriority > priority
        if(isCaller) {
          createConnectionHandle(webRTCConnection.createDataChannel("sendDataChannel"))
          webRTCConnection.createOffer { description: RTCSessionDescription =>
            webRTCConnection.setLocalDescription(description)
            sendViaSignaling(SessionDescription(js.JSON.stringify(description)))
          }
        } else {
          webRTCConnection.ondatachannel = { event: Event =>
            createConnectionHandle(event.asInstanceOf[RTCDataChannelEvent].channel) // WebRTC API typo?
          }
        }
      
      case IceCandidate(candidate) =>
        webRTCConnection.addIceCandidate(new RTCIceCandidate(
          js.JSON.parse(candidate).asInstanceOf[RTCIceCandidate]))

      case SessionDescription(description) =>
        val remoteDescription = new RTCSessionDescription(
            js.JSON.parse(description).asInstanceOf[RTCSessionDescriptionInit])
        if(isCaller) {
          webRTCConnection.setRemoteDescription(remoteDescription)
        } else {
          webRTCConnection.setRemoteDescription(remoteDescription)
          webRTCConnection.createAnswer { localDescription: RTCSessionDescription =>
            webRTCConnection.setLocalDescription(localDescription)
            sendViaSignaling(SessionDescription(js.JSON.stringify(localDescription)))
          }
        }
        
    }
  }
  
  private def createConnectionHandle(dc: RTCDataChannel): Unit = {
    new ConnectionHandle {
      private val promise = Promise[MessageListener]()
      private var poorMansBuffer: Future[MessageListener] = promise.future
      
      dc.onopen = { event: Event =>
        connectionPromise.success(this)
      }
      dc.onmessage = { event: RTCMessageEvent =>
        poorMansBuffer = poorMansBuffer.andThen {
          case Success(listener) =>
            listener.notify(event.data.toString())
        }
      }
      dc.onclose = { event: Event =>
        poorMansBuffer = poorMansBuffer.andThen {
          case Success(listener) =>
            listener.closed()
        }
      }
      dc.onerror = { event: Event =>
        poorMansBuffer = poorMansBuffer.andThen {
          case Success(listener) =>
            listener.closed()
        }
      }
      
      def handlerPromise: Promise[MessageListener] = promise
      def write(outboundPayload: String): Unit = dc.send(outboundPayload)
      def close(): Unit = dc.close()
    }
  }
}

private object OptionalMediaConstraint extends RTCOptionalMediaConstraint {
  override val DtlsSrtpKeyAgreement: js.Boolean = false
  override val RtpDataChannels: js.Boolean = false
}
private object DataChannelsConstraint extends RTCMediaConstraints {
  override val mandatory: RTCMediaOfferConstraints = null
  override val optional: js.Array[RTCOptionalMediaConstraint] = js.Array(OptionalMediaConstraint)
}

// //////////////////////////////////////

// private class WebRTCCalleeProxy(handlerProps: ActorRef => Props)
//     extends WebRTCPeersssdsdsds(handlerProps) {
//   var peer: ActorRef = _
  
//   def receivedSignalingChannel(signalingChannel: ActorRef): Unit = {
//     peer = signalingChannel
//     webRTCConnection.ondatachannel = { event: Event =>
//       setDataChannel(event.asInstanceOf[RTCDataChannelEvent].channel) // WebRTC API typo?
//     }
//   }

//   def receivedSessionDescription(remoteDescription: RTCSessionDescription): Unit = {
//     webRTCConnection.setRemoteDescription(remoteDescription)
//     webRTCConnection.createAnswer { localDescription: RTCSessionDescription =>
//       webRTCConnection.setLocalDescription(localDescription)
//       peer ! SessionDescription(js.JSON.stringify(localDescription))
//     }
//   }
// }


// //////////////////////////////////////

// private class WebRTCCallerProxy(calleeRef: ActorRef, handlerProps: ActorRef => Props)
//     extends WebRTCPeersssdsdsds(handlerProps) {
//   override def preStart(): Unit = {
//     super.preStart()
//     self ! SignalingChannel(calleeRef)
//     calleeRef ! SignalingChannel(self)
//   }
  
//   def receivedSignalingChannel(peer: ActorRef): Unit = {
//     setDataChannel(webRTCConnection.createDataChannel("sendDataChannel"))
//     webRTCConnection.createOffer { description: RTCSessionDescription =>
//       webRTCConnection.setLocalDescription(description)
//       peer ! SessionDescription(js.JSON.stringify(description))
//     }
//   }
  
//   def receivedSessionDescription(remoteDescription: RTCSessionDescription): Unit = {
//     webRTCConnection.setRemoteDescription(remoteDescription)
//   }
// }

// /////////////////////////////////////7

// private abstract class WebRTCPeersssdsdsds(handlerProps: ActorRef => Props) extends Actor {
//   RegisterWebRTCPicklers.registerPicklers()
//   var webRTCConnection: webkitRTCPeerConnection = _
//   var dataChannel: Option[RTCDataChannel] = None
//   var handlerActor: ActorRef = _

//   override def preStart(): Unit = {
//     webRTCConnection = new webkitRTCPeerConnection(null, DataChannelsConstraint)
//   }
  
//   override def postStop(): Unit = {
//     dataChannel.foreach(_.close())
//     webRTCConnection.close()
//   }

//   def receive = {
//     case SignalingChannel(peer: ActorRef) =>
//       webRTCConnection.onicecandidate = { event: RTCIceCandidateEvent =>
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
//       webRTCConnection.addIceCandidate(new RTCIceCandidate(js.JSON.parse(candidate).asInstanceOf[RTCIceCandidate]))
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
