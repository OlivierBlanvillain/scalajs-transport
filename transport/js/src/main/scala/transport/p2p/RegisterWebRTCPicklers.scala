package transport.p2p

import akka.actor._

import org.scalajs.spickling._
import org.scalajs.spickling.jsany._

case class Priority(value: Double)
case class IceCandidate(string: String)
case class SessionDescription(string: String)
case class SignalingChannel(peer: ActorRef)

object RegisterWebRTCPicklers {
  import PicklerRegistry.register

  register[Priority]
  register[IceCandidate]
  register[SessionDescription]
  register[SignalingChannel]

  def registerPicklers(): Unit = ()
}
