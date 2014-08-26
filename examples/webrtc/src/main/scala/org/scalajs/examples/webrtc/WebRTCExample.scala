package org.scalajs.examples.faulttolerance

import akka.actor._
import scala.scalajs.js
import js.annotation.JSExport
import akka.scalajs.p2p._
import akka.scalajs.jsapi._

@JSExport
object WebRTCExample {
  @JSExport
  def main(): Unit = {
    println("Hello!")
  }

  class Local extends Actor {
    private var peer: Option[ActorRef] = None
    
    override def receive = {
      case PeerProxy.WebRTCConnected(p) =>
        this.peer = Some(p)
        dataChannelSend.disabled = false
        dataChannelSend.focus()
        dataChannelSend.placeholder = ""
        sendButton.disabled = false
        closeButton.disabled = false
      case SendClicked =>
        val data = dataChannelSend.value.toString
        peer map (_ ! data)
        println("Sent data: " + data)
    }
  }

  class Remote extends Actor {
    override def receive = {
      case PeerProxy.WebRTCConnected(p) =>
        ()
      case data: String =>
        println("Received message: " + data)
        document.getElementById("dataChannelReceive").value = data
    }
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
  
  val system = ActorSystem("WebRTCExample")
  var local: ActorRef = system.actorOf(Props(new Local), name = "local")
  var remote: ActorRef = system.actorOf(Props(new Remote), name = "remote")
  var callerProxy: ActorRef = null
  var calleeProxy: ActorRef = null

  def createConnection(): Unit = {
    callerProxy = system.actorOf(Props(new CallerProxy(local)), name = "caller")
    calleeProxy = system.actorOf(Props(new CalleeProxy(remote)), name = "callee")
    callerProxy ! PeerProxy.SignalingChannel(calleeProxy)
    calleeProxy ! PeerProxy.SignalingChannel(callerProxy)
    startButton.disabled = true
    closeButton.disabled = false
  }

  def closeDataChannels(): Unit = {
    callerProxy ! PoisonPill
    calleeProxy ! PoisonPill
    println("Closed peer connections")
    startButton.disabled = false
    sendButton.disabled = true
    closeButton.disabled = true
    dataChannelSend.value = ""
    dataChannelReceive.value = ""
    dataChannelSend.disabled = true
    dataChannelSend.placeholder = "Press Start, enter some text, then press Send."
  }
  
  case object SendClicked

  def sendData(): Unit = local ! SendClicked
}
