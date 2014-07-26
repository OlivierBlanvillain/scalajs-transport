package akka.scalajs.p2p

import akka.actor._
import akka.scalajs.jsapi._
import akka.scalajs.wscommon.AbstractProxy
import org.scalajs.spickling._
import org.scalajs.spickling.jsany._
import scala.scalajs.js

object PeerProxy {
  case object ConnectionError
  case class WebRTCConnected(entryPointRef: ActorRef)
  case class IceCandidate(candidate: RTCIceCandidate)
  case class SessionDescription(description: RTCSessionDescription)
  case class SignalingChannel(peer: ActorRef)
}

abstract class PeerProxy(connectedHandler: ActorRef) extends AbstractProxy {
  
  import AbstractProxy._
  import PeerProxy._

  type PickleType = js.Any
  implicit protected def pickleBuilder: PBuilder[PickleType] = JSPBuilder
  implicit protected def pickleReader: PReader[PickleType] = JSPReader
  
  var peerConnection: webkitRTCPeerConnection = _
  var dataChannel: Option[RTCDataChannel] = None
  
  override def preStart() = {
    super.preStart()
    peerConnection = new webkitRTCPeerConnection(null, DataChannelsConstraint)
  }

  override def postStop() = {
    super.postStop()
    dataChannel.foreach(_.close())
    peerConnection.close()
  }
  
  override def receive = super.receive.orElse[Any, Unit] {
    case SignalingChannel(peer: ActorRef) =>
      peerConnection.onicecandidate = { event: RTCIceCandidateEvent =>
        println("Ice callback")
        if(event.candidate != null) {
          peer ! IceCandidate(event.candidate)
          println("Ice candidate: ")
          println(event.candidate.candidate)
        }
      }
      receivedSignalingChannel(peer)
    case SessionDescription(description) =>
      receivedSessionDescription(description)
    case IceCandidate(candidate) =>
      peerConnection.addIceCandidate(candidate)
    // case Welcome(entryPointRef) =>
    //   connectedHandler ! WebRTCConnected(entryPointRef)
    case ConnectionError =>
      throw new akka.AkkaException("WebRTC connection error")
  }
  
  override def receiveFromPeer = super.receiveFromPeer.orElse[Any, Unit] {
    case Welcome(entryPointRef) =>
      connectedHandler ! WebRTCConnected(entryPointRef)
      // val msg = WebSocketConnected(entryPointRef)
      // if (connectedHandler eq null) context.parent ! msg
      // else connectedHandler ! msg
  }

  
  protected def receivedSignalingChannel(peer: ActorRef): Unit

  protected def receivedSessionDescription(description: RTCSessionDescription): Unit

  protected def setDataChannelCallbacks(dc: RTCDataChannel) = {
    dc.onopen = { (event: Event) =>
      self ! SendToPeer(Welcome(connectedHandler))
    }
    dc.onmessage = { (event: RTCMessageEvent) =>
      // Not to sure why this is needed...
      self ! IncomingMessage(js.JSON.parse(event.data.toString()))
    }
    dc.onclose = { (event: Event) =>
      self ! ConnectionClosed
    }
    dc.onerror = { (event: Event) =>
      self ! ConnectionError
    }
  }

  override protected def sendPickleToPeer(pickle: PickleType): Unit = {
    dataChannel.foreach(_.send(js.JSON.stringify(pickle)))
  }
  
}

object DataChannelsConstraint extends RTCMediaConstraints {
  override val mandatory: RTCMediaOfferConstraints = null
  override val optional: js.Array[RTCOptionalMediaConstraint] = js.Array(OptionalMediaConstraint)
}
object OptionalMediaConstraint extends RTCOptionalMediaConstraint {
  override val DtlsSrtpKeyAgreement: js.Boolean = false
  override val RtpDataChannels: js.Boolean = true
}
