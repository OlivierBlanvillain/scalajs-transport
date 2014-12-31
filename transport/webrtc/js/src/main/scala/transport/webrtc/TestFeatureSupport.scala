package transport.webrtc

import scala.util._
import transport.jsapi._

object TestFeatureSupport {
  /** Chrome only ATM. */
  def webRTC(): Boolean = {
    Try(new webkitRTCPeerConnection(null).iceConnectionState).isSuccess
  }
}
