package org.scalajs.examples.faulttolerance

import scala.language.postfixOps

import akka.actor._
import akka.actor.SupervisorStrategy._
import akka.util.Timeout
import akka.event.LoggingReceive
import akka.pattern.Ask.ask
import akka.pattern.PipeTo.pipe
import scala.concurrent.duration._

import scala.scalajs.js
import js.annotation.JSExport

import akka.scalajs.p2p._
import akka.scalajs.jsapi._

@JSExport
object WebRTCExample {
  @JSExport
  def main(): Unit = {
    // val system = ActorSystem("WebRTCExample")
    // val peerA = system.actorOf(Props(new PeerProxy), name = "peerA")
    // val peerB = system.actorOf(Props(new PeerProxy), name = "peerB")
    // println("Hello!")
  }
  
  val document = js.Dynamic.global.document
  val startButton = document.getElementById("startButton")
  val sendButton = document.getElementById("sendButton")
  val closeButton = document.getElementById("closeButton")
  val dataChannelSend = document.getElementById("dataChannelSend")
  val dataChannelReceive = document.getElementById("dataChannelReceive")
  
  startButton.disabled = false
  sendButton.disabled = true
  closeButton.disabled = true
  startButton.onclick = createConnection _
  sendButton.onclick = sendData _
  closeButton.onclick = closeDataChannels _
  
  var localPeerConnection: webkitRTCPeerConnection = null
  var remotePeerConnection: webkitRTCPeerConnection = null
  var sendChannel: RTCDataChannel = null
  var receiveChannel: RTCDataChannel = null

  def createConnection(): Unit = {
    localPeerConnection = new webkitRTCPeerConnection(null, DataChannelsConstraint)
    println("Created local peer connection object localPeerConnection")
    
    sendChannel = localPeerConnection.createDataChannel("sendDataChannel")
    println("Created send data channel")

    localPeerConnection.onicecandidate = gotLocalCandidate _
    sendChannel.onopen = handleSendChannelStateChange _
    sendChannel.onclose = handleSendChannelStateChange _

    remotePeerConnection = new webkitRTCPeerConnection(null, DataChannelsConstraint)
    println("Created remote peer connection object remotePeerConnection")

    remotePeerConnection.onicecandidate = gotRemoteIceCandidate _
    remotePeerConnection.ondatachannel = gotReceiveChannel _

    localPeerConnection.createOffer(gotLocalDescription _)
    startButton.disabled = true
    closeButton.disabled = false
  }

  def sendData(): Unit = {
    val data = dataChannelSend.value
    sendChannel.send(data.toString)
    println("Sent data: " + data.toString)
  }

  def closeDataChannels(): Unit = {
    println("Closing data channels")
    sendChannel.close()

    println("Closed data channel with label: " + sendChannel.label)
    receiveChannel.close()
    
    println("Closed data channel with label: " + receiveChannel.label)
    localPeerConnection.close()
    
    remotePeerConnection.close()
    localPeerConnection = null
    remotePeerConnection = null
    println("Closed peer connections")
    
    startButton.disabled = false
    sendButton.disabled = true
    closeButton.disabled = true
    dataChannelSend.value = ""
    dataChannelReceive.value = ""
    dataChannelSend.disabled = true
    dataChannelSend.placeholder = "Press Start, enter some text, then press Send."
  }
  
  def gotLocalDescription(desc: RTCSessionDescription): Unit = {
    localPeerConnection.setLocalDescription(desc)
    println("Offer from localPeerConnection \n" + desc.sdp)
    remotePeerConnection.setRemoteDescription(desc)
    remotePeerConnection.createAnswer(gotRemoteDescription _)
  }
  
  def gotRemoteDescription(desc: RTCSessionDescription): Unit = {
    remotePeerConnection.setLocalDescription(desc)
    println("Answer from remotePeerConnection \n" + desc.sdp)
    localPeerConnection.setRemoteDescription(desc)
  }

  def gotLocalCandidate(event: RTCIceCandidateEvent): Unit = {
    println("local ice callback")
    if(event.candidate != null) {
      remotePeerConnection.addIceCandidate(event.candidate)
      println("Local ICE candidate: \n" + event.candidate.candidate)
    }
  }

  def gotRemoteIceCandidate(event: RTCIceCandidateEvent): Unit = {
    println("remote ice callback")
    if(event.candidate != null) {
      localPeerConnection.addIceCandidate(event.candidate)
      println("Remote ICE candidate: \n " + event.candidate.candidate)
    }
  }

  def gotReceiveChannel(event: Event): Unit = {
    println("Receive Channel Callback")
    receiveChannel = event.asInstanceOf[RTCDataChannelEvent].channel // WebRTC API typo?
    receiveChannel.onmessage = handleMessage _
    receiveChannel.onopen = handleReceiveChannelStateChange _
    receiveChannel.onclose = handleReceiveChannelStateChange _
  }

  def handleMessage(event: RTCMessageEvent): Unit = {
    println("Received message: " + event.data)
    document.getElementById("dataChannelReceive").value = event.data
  }

  def handleSendChannelStateChange(event: Event): Unit = {
    var readyState = sendChannel.readyState
    println("Send channel state is: " + readyState)
    if(readyState == "open") {
      dataChannelSend.disabled = false
      dataChannelSend.focus()
      dataChannelSend.placeholder = ""
      sendButton.disabled = false
      closeButton.disabled = false
    } else {
      dataChannelSend.disabled = true
      sendButton.disabled = true
      closeButton.disabled = true
    }
  }

  def handleReceiveChannelStateChange(event: Event): Unit = {
    var readyState = receiveChannel.readyState
    println("Receive channel state is: " + readyState)
  }

}
