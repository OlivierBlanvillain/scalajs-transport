package client

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js
import org.scalajs.jquery.{jQuery => jQ, _}

import akka.actor._

import models._
import transport.client._
import transport.akka._
import SockJSClient.addressFromPlayRoute

import scalajs.concurrent.JSExecutionContext.Implicits.runNow

@JSExport("Client")
object Main {
  RegisterPicklers.registerPicklers()

  implicit val system = ActorSystem("chat-client")

  @JSExport
  def startup(): Unit = {
    ActorWrapper(new SockJSClient()).connectWithActor(addressFromPlayRoute())(DemoActor.props)
  }
}

class DemoActor(out: ActorRef) extends Actor {
  override def postStop() = {
    jQ("#msgtext").prop("disabled", true)
    jQ("#discussion").append("<hr>")
  }

  override def receive: Receive = {
    case Connected(peer) =>
      jQ("#msgform").submit { (event: JQueryEventObject) =>
        event.preventDefault()
        self ! Submit
      }
      jQ("#spinner").hide()
      jQ("#msgform").show()
      context.watch(peer)
      context.become(connected(peer))
  }

  def connected(peer: ActorRef): Receive = {
    case Submit =>
      val text = jQ("#msgtext").value().toString
      if(!text.isEmpty) {
        jQ("#msgtext").value("")
        peer ! Msg(text)
        Discussion.appendMy(text)
      }
    
    case Msg(text) =>
      Discussion.appendHis(text)
    
    case Terminated(ref) if ref == peer =>
      context.stop(self)
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
