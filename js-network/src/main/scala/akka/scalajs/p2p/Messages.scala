package akka.scalajs.p2p

import akka.actor._
import akka.scalajs.jsapi._
import scala.scalajs.js

private case object ConnectionError
private case class WebRTCConnected(entryPointRef: ActorRef)
private case class IceCandidate(candidate: RTCIceCandidate)
private case class SessionDescription(description: RTCSessionDescription)
private case class SignalingChannel(peer: ActorRef)

private object RegisterPicklers {
  import org.scalajs.spickling._
  import PicklerRegistry.register

  register(ConnectionError)
  register[WebRTCConnected]
  register[IceCandidate]
  register[SessionDescription]
  register[SignalingChannel]

  def registerPicklers(): Unit = ()
}
