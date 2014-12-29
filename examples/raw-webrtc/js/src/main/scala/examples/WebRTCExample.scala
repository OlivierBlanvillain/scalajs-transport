package examples

import akka.actor._

import scala.scalajs.js
import js.annotation.JSExport

import transport._
import transport.akka._
import transport.javascript._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object Page {
  val document = js.Dynamic.global.document
  val startButton = document.getElementById("startButton")
  val sendButton = document.getElementById("sendButton")
  val closeButton = document.getElementById("closeButton")
  val dataChannelSend = document.getElementById("dataChannelSend")
  val dataChannelReceive = document.getElementById("dataChannelReceive")
}
import Page._

@JSExport("WebRTCExample")
object WebRTCExample {
  @JSExport("main")
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
    val (connection1, connection2) = ProxyConnectionHandle.newConnectionsPair()
    
    ActorWrapper(new WebRTCClient()).connectWithActor(connection1) { out =>
      Props(new Local(out))
    }

    ActorWrapper(new WebRTCClient()).connectWithActor(connection2) { out =>
      Props(new Remote(out))
    }

    startButton.disabled = false
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
  def receive = {
    case data: String =>
      println("Sent data: " + data)
      out ! data
  }
}

class Remote(out: ActorRef) extends Actor {
  override def preStart = {
    WebRTCExample.remote = self
  }
  def receive = {
    case data: String =>
      println("Received message: " + data)
      document.getElementById("dataChannelReceive").value = data
  }
}
