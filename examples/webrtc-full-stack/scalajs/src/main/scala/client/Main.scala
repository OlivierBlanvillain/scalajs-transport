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
  val me = User(System.currentTimeMillis()) // Poor man's UUID
  
  override def preStart() = {
    Page.onSubmit {
      out ! Message(Page.getText(), me)
      Page.clearText()
    }
  }
  
  def receive = {
    case Message(text, user, timestamp) if user == me =>
      Page.appendMine(text)
    case Message(text, user, timestamp) =>
      Page.appendHis(text)
  }
}

object Page {
  import org.scalajs.jquery.{jQuery => jQ, _}
  
  def onSubmit(f: => Unit): Unit = {
    jQ("#msgform").submit { (event: JQueryEventObject) =>
      event.preventDefault()
      f
    }
  }
    
  def getText(): String = jQ("#msgtext").value().toString
  
  def clearText(): Unit = jQ("#msgtext").value("")
  
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
  def appendMine = append("self") _
  def appendHis = append("other") _
}

case class WebSocketClient(url: String)(implicit system: ActorSystem) {
  def connectWithActor(handlerProps: ActorRef => Props) =
    system.actorOf(Props(new WebSocketClientProxy(url, handlerProps)))
}
class WebSocketClientProxy(url: String, handlerProps: ActorRef => Props) extends Actor {
  var webSocket: WebSocket = _

  override def preStart() = {
    val handlerActor = context.watch(context.actorOf(handlerProps(self)))
    
    webSocket = new WebSocket(url)
    webSocket.addEventListener("message", { (event: Event) =>
      val pickle = js.JSON.parse(event.asInstanceOf[MessageEvent].data.toString())
      val message = PicklerRegistry.unpickle(pickle.asInstanceOf[js.Any])
      handlerActor ! message
    }, useCapture = false)
    webSocket.addEventListener("close", { (event: Event) =>
      context.stop(self)
    }, useCapture = false)
    webSocket.addEventListener("error", { (event: Event) =>
      context.stop(self)
    }, useCapture = false)
  }

  override def postStop() = {
    webSocket.close()
  }

  def receive = {
    case _: Terminated =>
      context.stop(self)
    case message =>
      val pickle = PicklerRegistry.pickle(message)
      webSocket.send(js.JSON.stringify(pickle))
  }
}
