package transport.jsapi

import scala.scalajs.js

class RTCConfiguration extends js.Object {
  var iceServers: js.Array[RTCIceServer] = js.native
}

object RTCConfiguration extends js.Object {
}

class RTCIceServer extends js.Object {
  var url: String = js.native
  var credential: String = js.native
}

object RTCIceServer extends js.Object {
}

class mozwebkitRTCPeerConnection protected () extends webkitRTCPeerConnection {
  def this(settings: webkitRTCPeerConnectionConfig, constraints: RTCMediaConstraints = js.native) = this()
}

object mozwebkitRTCPeerConnection extends js.Object {
}

class webkitwebkitRTCPeerConnection protected () extends webkitRTCPeerConnection {
  def this(settings: webkitRTCPeerConnectionConfig, constraints: RTCMediaConstraints = js.native) = this()
}

object webkitwebkitRTCPeerConnection extends js.Object {
}

trait RTCOptionalMediaConstraint extends js.Object {
  val DtlsSrtpKeyAgreement: Boolean = js.native
  val RtpDataChannels: Boolean = js.native
}

trait RTCMediaConstraints extends js.Object {
  val mandatory: RTCMediaOfferConstraints = js.native
  val optional: js.Array[RTCOptionalMediaConstraint] = js.native
}

trait RTCMediaOfferConstraints extends js.Object {
  var OfferToReceiveAudio: Boolean = js.native
  var OfferToReceiveVideo: Boolean = js.native
}

trait RTCSessionDescriptionInit extends js.Object {
  var `type`: String = js.native
  var sdp: String = js.native
}

class RTCSessionDescription protected () extends js.Object {
  def this(descriptionInitDict: RTCSessionDescriptionInit = js.native) = this()
  var `type`: String = js.native
  var sdp: String = js.native
}

object RTCSessionDescription extends js.Object {
}

trait RTCDataChannelInit extends js.Object {
  var ordered: Boolean = js.native
  var maxPacketLifeTime: Int = js.native
  var maxRetransmits: Int = js.native
  var protocol: String = js.native
  var negotiated: Boolean = js.native
  var id: Int = js.native
}

trait RTCMessageEvent extends js.Object {
  var data: js.Any = js.native
}

class RTCDataChannel extends EventTarget {
  var label: String = js.native
  var reliable: Boolean = js.native
  var readyState: String = js.native
  var bufferedAmount: Int = js.native
  var binaryType: String = js.native
  var onopen: js.Function1[Event, _] = js.native
  var onerror: js.Function1[Event, _] = js.native
  var onclose: js.Function1[Event, _] = js.native
  var onmessage: js.Function1[RTCMessageEvent, _] = js.native
  def close(): Unit = js.native
  def send(data: String): Unit = js.native
}

object RTCDataChannel extends js.Object {
}

class RTCDataChannelEvent protected () extends Event {
  def this(eventInitDict: RTCDataChannelEventInit) = this()
  var channel: RTCDataChannel = js.native
}

object RTCDataChannelEvent extends js.Object {
}

trait RTCIceCandidateEvent extends Event {
  var candidate: RTCIceCandidate = js.native
}

trait RTCMediaStreamEvent extends Event {
  var stream: MediaStream = js.native
}

trait EventInit extends js.Object {
}

trait RTCDataChannelEventInit extends EventInit {
  var channel: RTCDataChannel = js.native
}

trait RTCStatsReport extends js.Object {
  def stat(id: String): String = js.native
}


class webkitRTCPeerConnection protected () extends js.Object {
  def this(configuration: RTCConfiguration, constraints: RTCMediaConstraints = js.native) = this()
  def createOffer(successCallback: RTCSessionDescriptionCallback, failureCallback: webkitRTCPeerConnectionErrorCallback = js.native, constraints: RTCMediaConstraints = js.native): Unit = js.native
  def createAnswer(successCallback: RTCSessionDescriptionCallback, failureCallback: webkitRTCPeerConnectionErrorCallback = js.native, constraints: RTCMediaConstraints = js.native): Unit = js.native
  def setLocalDescription(description: RTCSessionDescription, successCallback: RTCVoidCallback = js.native, failureCallback: webkitRTCPeerConnectionErrorCallback = js.native): Unit = js.native
  var localDescription: RTCSessionDescription = js.native
  def setRemoteDescription(description: RTCSessionDescription, successCallback: RTCVoidCallback = js.native, failureCallback: webkitRTCPeerConnectionErrorCallback = js.native): Unit = js.native
  var remoteDescription: RTCSessionDescription = js.native
  var signalingState: String = js.native
  def updateIce(configuration: RTCConfiguration = js.native, constraints: RTCMediaConstraints = js.native): Unit = js.native
  def addIceCandidate(candidate: RTCIceCandidate): Unit = js.native
  var iceGatheringState: String = js.native
  var iceConnectionState: String = js.native
  def getLocalStreams(): js.Array[MediaStream] = js.native
  def getRemoteStreams(): js.Array[MediaStream] = js.native
  def createDataChannel(label: String = js.native, dataChannelDict: RTCDataChannelInit = js.native): RTCDataChannel = js.native
  var ondatachannel: js.Function1[Event, _] = js.native
  def addStream(stream: MediaStream, constraints: RTCMediaConstraints = js.native): Unit = js.native
  def removeStream(stream: MediaStream): Unit = js.native
  def close(): Unit = js.native
  var onnegotiationneeded: js.Function1[Event, _] = js.native
  var onconnecting: js.Function1[Event, _] = js.native
  var onopen: js.Function1[Event, _] = js.native
  var onaddstream: js.Function1[RTCMediaStreamEvent, _] = js.native
  var onremovestream: js.Function1[RTCMediaStreamEvent, _] = js.native
  var onstatechange: js.Function1[Event, _] = js.native
  var onicechange: js.Function1[Event, _] = js.native
  var onicecandidate: js.Function1[RTCIceCandidateEvent, _] = js.native
  var onidentityresult: js.Function1[Event, _] = js.native
  var onsignalingstatechange: js.Function1[Event, _] = js.native
  var getStats: js.Function2[RTCStatsCallback, webkitRTCPeerConnectionErrorCallback, _] = js.native
}

object webkitRTCPeerConnection extends js.Object {
}

class RTCIceCandidate protected () extends js.Object {
  def this(candidateInitDict: RTCIceCandidate = js.native) = this()
  var candidate: String = js.native
  var sdpMid: String = js.native
  var sdpMLineIndex: Int = js.native
}

object RTCIceCandidate extends js.Object {
}

class RTCIceCandidateInit extends js.Object {
  var candidate: String = js.native
  var sdpMid: String = js.native
  var sdpMLineIndex: Int = js.native
}

object RTCIceCandidateInit extends js.Object {
}

class PeerConnectionIceEvent extends js.Object {
  var peer: webkitRTCPeerConnection = js.native
  var candidate: RTCIceCandidate = js.native
}

object PeerConnectionIceEvent extends js.Object {
}

class webkitRTCPeerConnectionConfig extends js.Object {
  var iceServers: js.Array[RTCIceServer] = js.native
}

object webkitRTCPeerConnectionConfig extends js.Object {
}
