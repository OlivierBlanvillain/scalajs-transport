package client

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js
import org.scalajs.jquery.{jQuery => jQ, _}

import akka.actor._
import akka.scalajs.client.WebSocketClient

import models._

@JSExport("Client")
object Main {
  RegisterPicklers.registerPicklers()

  implicit val system = ActorSystem("chat-client")

  @JSExport
  def startup(): Unit = {
    WebSocketClient("ws://localhost:9000/websocket").connectWithActor(DemoActor.props)
  }
}

class DemoActor(out: ActorRef) extends Actor {
  override def postStop() = {
    jQ("#msgtext").prop("disabled", true)
    jQ("#discussion").append("<hr>")
  }

  def receive = {
    case Message(text) =>
      Discussion.appendHis(text)
    case Submit =>
      val text = jQ("#msgtext").value().toString
      if(!text.isEmpty) {
        jQ("#msgtext").value("")
        out ! Message(text)
        Discussion.appendMy(text)
      }
    case PeerFound =>
      jQ("#msgform").submit { (event: JQueryEventObject) =>
        event.preventDefault()
        self ! Submit
      }
      jQ("#spinner").hide()
      jQ("#msgform").show()
  }
  
  object Submit
}
object DemoActor {
  def props(out: ActorRef) = Props(new DemoActor(out))
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
