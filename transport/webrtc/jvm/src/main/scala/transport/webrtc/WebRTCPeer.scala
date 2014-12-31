package transport.webrtc

import scala.concurrent._
import transport._

private class WebRTCPeer(
    signalingChannel: ConnectionHandle,
    priority: Double=0)(
    implicit ec: ExecutionContext) {

  def future: Future[ConnectionHandle] =
    Future.failed(new UnsupportedOperationException("WebRTC is not available on the JVM."))
}

private object WebRTCPeer {
  def supportsWebRTC(): Boolean = false
}
