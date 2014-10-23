package transport

import scala.scalajs.js

package object jsapi {
  type RTCVoidCallback = js.Function0[Unit]
  type RTCSessionDescriptionCallback = js.Function1[RTCSessionDescription, Unit]
  type webkitRTCPeerConnectionErrorCallback = js.Function1[js.Object, Unit] // TODO: DOMError arg
  type RTCStatsCallback = js.Function1[RTCStatsReport, Unit]

  type Event = org.scalajs.dom.Event
  type EventTarget = org.scalajs.dom.EventTarget
  type MessageEvent = org.scalajs.dom.MessageEvent
  type CloseEvent = org.scalajs.dom.CloseEvent
  type ErrorEvent = org.scalajs.dom.ErrorEvent
  type WebSocket = org.scalajs.dom.WebSocket
}
