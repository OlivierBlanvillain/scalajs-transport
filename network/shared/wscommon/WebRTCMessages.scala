package akka.scalajs.common

import akka.actor._

case class IceCandidate(string: String)
case class SessionDescription(string: String)
case class SignalingChannel(peer: ActorRef)

object RegisterWebRTCPicklers {
  import org.scalajs.spickling._
  import PicklerRegistry.register

  register[IceCandidate]
  register[SessionDescription]
  register[SignalingChannel]

  def registerPicklers(): Unit = ()
}
