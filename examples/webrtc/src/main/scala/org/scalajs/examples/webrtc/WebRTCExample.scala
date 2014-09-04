package org.scalajs.examples.faulttolerance

import akka.actor._
import scala.scalajs.js
import js.annotation.JSExport
import akka.scalajs.p2p._
import akka.scalajs.jsapi._
import Page._

@JSExport
object WebRTCExample {
  @JSExport
  def main(): Unit = {
    startButton.disabled = false
    sendButton.disabled = true
    closeButton.disabled = true
    startButton.onclick = createConnection _
    sendButton.onclick = sendData _
    closeButton.onclick = closeDataChannels _
  }

  implicit val system = ActorSystem("WebRTCExample")
  var local: ActorRef = _
  var remote: ActorRef = _

  def createConnection(): Unit = {
    val calleeRef = WebRTCCallee.answerWithActor { out =>
      Props(new Local(out))
    }

    WebRTCCaller(calleeRef).callWithActor { out =>
      Props(new Remote(out))
    }

    startButton.disabled = true
    closeButton.disabled = false
  }

  def closeDataChannels(): Unit = {
    local ! PoisonPill
    remote ! PoisonPill

    startButton.disabled = false
    sendButton.disabled = true
    closeButton.disabled = true
    dataChannelSend.value = ""
    dataChannelReceive.value = ""
    dataChannelSend.disabled = true
    dataChannelSend.placeholder = "Press Start, enter some text, then press Send."
  }
  
  def sendData(): Unit = {
    val data = dataChannelSend.value.toString
    local ! data
  }
}

class Local(out: ActorRef) extends Actor {
  override def preStart = {
    WebRTCExample.local = self
    dataChannelSend.disabled = false
    dataChannelSend.focus()
    dataChannelSend.placeholder = ""
    sendButton.disabled = false
    closeButton.disabled = false
  }
  override def receive = {
    case data: String =>
      println("Sent data: " + data)
      out ! data
  }
}

class Remote(out: ActorRef) extends Actor {
  override def preStart = {
    WebRTCExample.remote = self
  }
  override def receive = {
    case data: String =>
      println("Received message: " + data)
      document.getElementById("dataChannelReceive").value = data
  }
}

object Page {
  val document = js.Dynamic.global.document
  val startButton = document.getElementById("startButton")
  val sendButton = document.getElementById("sendButton")
  val closeButton = document.getElementById("closeButton")
  val dataChannelSend = document.getElementById("dataChannelSend")
  val dataChannelReceive = document.getElementById("dataChannelReceive")
}
