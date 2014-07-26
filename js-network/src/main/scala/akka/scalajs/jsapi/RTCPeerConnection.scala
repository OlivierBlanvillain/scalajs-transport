package akka.scalajs.jsapi

import scala.scalajs.js

class RTCConfiguration extends js.Object {
  var iceServers: js.Array[RTCIceServer] = ???
}

object RTCConfiguration extends js.Object {
}

class RTCIceServer extends js.Object {
  var url: js.String = ???
  var credential: js.String = ???
}

object RTCIceServer extends js.Object {
}

class mozwebkitRTCPeerConnection protected () extends webkitRTCPeerConnection {
  def this(settings: webkitRTCPeerConnectionConfig, constraints: RTCMediaConstraints = ???) = this()
}

object mozwebkitRTCPeerConnection extends js.Object {
}

class webkitwebkitRTCPeerConnection protected () extends webkitRTCPeerConnection {
  def this(settings: webkitRTCPeerConnectionConfig, constraints: RTCMediaConstraints = ???) = this()
}

object webkitwebkitRTCPeerConnection extends js.Object {
}

trait RTCOptionalMediaConstraint extends js.Object {
  val DtlsSrtpKeyAgreement: js.Boolean = ???
  val RtpDataChannels: js.Boolean = ???
}

trait RTCMediaConstraints extends js.Object {
  val mandatory: RTCMediaOfferConstraints = ???
  val optional: js.Array[RTCOptionalMediaConstraint] = ???
}

trait RTCMediaOfferConstraints extends js.Object {
  var OfferToReceiveAudio: js.Boolean = ???
  var OfferToReceiveVideo: js.Boolean = ???
}

trait RTCSessionDescriptionInit extends js.Object {
  var `type`: js.String = ???
  var sdp: js.String = ???
}

class RTCSessionDescription protected () extends js.Object {
  def this(descriptionInitDict: RTCSessionDescriptionInit = ???) = this()
  var `type`: js.String = ???
  var sdp: js.String = ???
}

object RTCSessionDescription extends js.Object {
}

trait RTCDataChannelInit extends js.Object {
  var ordered: js.Boolean = ???
  var maxPacketLifeTime: js.Number = ???
  var maxRetransmits: js.Number = ???
  var protocol: js.String = ???
  var negotiated: js.Boolean = ???
  var id: js.Number = ???
}

trait RTCMessageEvent extends js.Object {
  var data: js.Any = ???
}

class RTCDataChannel extends EventTarget {
  var label: js.String = ???
  var reliable: js.Boolean = ???
  var readyState: js.String = ???
  var bufferedAmount: js.Number = ???
  var binaryType: js.String = ???
  var onopen: js.Function1[Event, Unit] = ???
  var onerror: js.Function1[Event, Unit] = ???
  var onclose: js.Function1[Event, Unit] = ???
  var onmessage: js.Function1[RTCMessageEvent, Unit] = ???
  def close(): Unit = ???
  def send(data: js.String): Unit = ???
}

object RTCDataChannel extends js.Object {
}

class RTCDataChannelEvent protected () extends Event {
  def this(eventInitDict: RTCDataChannelEventInit) = this()
  var channel: RTCDataChannel = ???
}

object RTCDataChannelEvent extends js.Object {
}

trait RTCIceCandidateEvent extends Event {
  var candidate: RTCIceCandidate = ???
}

trait RTCMediaStreamEvent extends Event {
  var stream: MediaStream = ???
}

trait EventInit extends js.Object {
}

trait RTCDataChannelEventInit extends EventInit {
  var channel: RTCDataChannel = ???
}

trait RTCStatsReport extends js.Object {
  def stat(id: js.String): js.String = ???
}


class webkitRTCPeerConnection protected () extends js.Object {
  def this(configuration: RTCConfiguration, constraints: RTCMediaConstraints = ???) = this()
  def createOffer(successCallback: RTCSessionDescriptionCallback, failureCallback: webkitRTCPeerConnectionErrorCallback = ???, constraints: RTCMediaConstraints = ???): Unit = ???
  def createAnswer(successCallback: RTCSessionDescriptionCallback, failureCallback: webkitRTCPeerConnectionErrorCallback = ???, constraints: RTCMediaConstraints = ???): Unit = ???
  def setLocalDescription(description: RTCSessionDescription, successCallback: RTCVoidCallback = ???, failureCallback: webkitRTCPeerConnectionErrorCallback = ???): Unit = ???
  var localDescription: RTCSessionDescription = ???
  def setRemoteDescription(description: RTCSessionDescription, successCallback: RTCVoidCallback = ???, failureCallback: webkitRTCPeerConnectionErrorCallback = ???): Unit = ???
  var remoteDescription: RTCSessionDescription = ???
  var signalingState: js.String = ???
  def updateIce(configuration: RTCConfiguration = ???, constraints: RTCMediaConstraints = ???): Unit = ???
  def addIceCandidate(candidate: RTCIceCandidate): Unit = ???
  var iceGatheringState: js.String = ???
  var iceConnectionState: js.String = ???
  def getLocalStreams(): js.Array[MediaStream] = ???
  def getRemoteStreams(): js.Array[MediaStream] = ???
  def createDataChannel(label: js.String = ???, dataChannelDict: RTCDataChannelInit = ???): RTCDataChannel = ???
  var ondatachannel: js.Function1[Event, Unit] = ???
  def addStream(stream: MediaStream, constraints: RTCMediaConstraints = ???): Unit = ???
  def removeStream(stream: MediaStream): Unit = ???
  def close(): Unit = ???
  var onnegotiationneeded: js.Function1[Event, Unit] = ???
  var onconnecting: js.Function1[Event, Unit] = ???
  var onopen: js.Function1[Event, Unit] = ???
  var onaddstream: js.Function1[RTCMediaStreamEvent, Unit] = ???
  var onremovestream: js.Function1[RTCMediaStreamEvent, Unit] = ???
  var onstatechange: js.Function1[Event, Unit] = ???
  var onicechange: js.Function1[Event, Unit] = ???
  var onicecandidate: js.Function1[RTCIceCandidateEvent, Unit] = ???
  var onidentityresult: js.Function1[Event, Unit] = ???
  var onsignalingstatechange: js.Function1[Event, Unit] = ???
  var getStats: js.Function2[RTCStatsCallback, webkitRTCPeerConnectionErrorCallback, Unit] = ???
}

object webkitRTCPeerConnection extends js.Object {
}

class RTCIceCandidate protected () extends js.Object {
  def this(candidateInitDict: RTCIceCandidate = ???) = this()
  var candidate: js.String = ???
  var sdpMid: js.String = ???
  var sdpMLineIndex: js.Number = ???
}

object RTCIceCandidate extends js.Object {
}

class RTCIceCandidateInit extends js.Object {
  var candidate: js.String = ???
  var sdpMid: js.String = ???
  var sdpMLineIndex: js.Number = ???
}

object RTCIceCandidateInit extends js.Object {
}

class PeerConnectionIceEvent extends js.Object {
  var peer: webkitRTCPeerConnection = ???
  var candidate: RTCIceCandidate = ???
}

object PeerConnectionIceEvent extends js.Object {
}

class webkitRTCPeerConnectionConfig extends js.Object {
  var iceServers: js.Array[RTCIceServer] = ???
}

object webkitRTCPeerConnectionConfig extends js.Object {
}
