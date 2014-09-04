package client

import scala.language.postfixOps

import scala.collection.mutable
import scala.concurrent.duration._

import akka.actor._
import akka.pattern.Ask.ask
import akka.scalajs.client._
import akka.event.LoggingReceive
import akka.util.Timeout
import akka.scalajs.jsapi.Timers
import akka.actor.PoisonPill

import models._

import scala.scalajs.js
import js.annotation.JSExport
import org.scalajs.dom
import akka.scalajs.jsapi._
import org.scalajs.spickling._
import org.scalajs.spickling.jsany._
import org.scalajs.jquery.{jQuery => jQ, _}

import akka.scalajs.client.WebSocketClient

@JSExport("Client")
object Main {
  RegisterPicklers.registerPicklers()

  implicit val system = ActorSystem("chat-client")

  @JSExport
  def startup(): Unit = {
    WebSocketClient("ws://localhost:9000/websocket").connectWithActor { out =>
      Props(new DemoActor(out))
    }
  }
}

class DemoActor(out: ActorRef) extends Actor {
  override def preStart() = {
    jQ("#msgform").submit { (event: JQueryEventObject) =>
      event.preventDefault()
      self ! Submit
    }
  }
  
  override def postStop() = {
    jQ("#msgtext").prop("disabled", true)
    jQ("#discussion").append("<hr>")
  }

  def receive = {
    case PeerFound1 =>
      // callerProxy = system.actorOf(Props(new CallerProxy(local)), name = "caller")
      // callerProxy ! PeerProxy.SignalingChannel(calleeProxy)
      jQ("#spinner").hide()
      jQ("#msgform").show()
    case PeerFound2 =>
      // calleeProxy = system.actorOf(Props(new CalleeProxy(remote)), name = "callee")
      // calleeProxy ! PeerProxy.SignalingChannel(callerProxy)
      jQ("#spinner").hide()
      jQ("#msgform").show()
    case Message(text) =>
      Discussion.appendHis(text)
    case Submit =>
      val text = jQ("#msgtext").value().toString
      if(!text.isEmpty) {
        jQ("#msgtext").value("")
        out ! Message(text)
        Discussion.appendMy(text)
      }
  }
}

object Discussion {
  def appendMy = append("self") _
  def appendHis = append("other") _
  private def append(cssClass: String)(message: String): Unit = {
    val discussion = jQ("#discussion")
    discussion.append(
      s"""<li class="$cssClass">
         |  <div class="avatar"></div>
         |  <div class="messages">
         |    <p>$message</p>
         |  </div>
         |</li>
      """.stripMargin)
    discussion.scrollTop(discussion.prop("scrollHeight").asInstanceOf[Int]) 
  }
}

object Submit
