package transport.webrtc

import scala.util._
import transport.jsapi._

object TestFeatureSupport {
  def webRTC(): Boolean = webkitRTC || mozRTC
  lazy val webkitRTC = Try(new webkitRTCPeerConnection(null).iceConnectionState).isSuccess
  lazy val mozRTC = Try(new mozRTCPeerConnection(null).iceConnectionState).isSuccess
}
