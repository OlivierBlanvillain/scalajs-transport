package akka.scalajs

import scala.scalajs.js

package object jsapi {
  type RTCVoidCallback = js.Function0[Unit]
  type RTCSessionDescriptionCallback = js.Function1[RTCSessionDescription, Unit]
  type webkitRTCPeerConnectionErrorCallback = js.Function1[js.Object, Unit] // TODO: DOMError arg
  type RTCStatsCallback = js.Function1[RTCStatsReport, Unit]  
}
