package client

import scala.scalajs.js.annotation.JSExport
import scala.concurrent.Future
import scala.scalajs.js
import org.scalajs.jquery.{ jQuery => jQ, _ }

import akka.actor._

import models._
import transport._
import transport.client._
import transport.akka._
import transport.p2p._
import SockJSClient.addressFromPlayRoute

import scalajs.concurrent.JSExecutionContext.Implicits.runNow

@JSExport("Client")
object Main {
  RegisterPicklers.registerPicklers()

  implicit val system = ActorSystem("chat-client")

  @JSExport
  def startup(): Unit = {
    ActorWrapper(new SockJSClient()).connectWithActor(addressFromPlayRoute())(
      EstablishRtcActor.props)
  }
}

class EstablishRtcActor(out: ActorRef) extends Actor {
  def receive: Receive = {
    case Connected(peer) =>
      val (actorConnection, futureConnection) = ActorToConnection(context.system)
      use(futureConnection)
      peer ! actorConnection

    case remoteActorConnection: ActorRef =>
      val (actorConnection, futureConnection) = ActorToConnection(context.system)
      actorConnection ! remoteActorConnection
      remoteActorConnection ! actorConnection
      use(futureConnection)
  }
  
  def use(futureConnection: Future[ConnectionHandle]): Unit = {
    futureConnection.foreach { signalingChannel =>
      val futureRTC = new WebRTCTransport().connect(signalingChannel)
      futureRTC.foreach { connection =>
        context.actorOf(ConnectionToActor.props(connection, DemoActor.props))
      }
    }
  }
}
object EstablishRtcActor {
  def props(out: ActorRef) = Props(new EstablishRtcActor(out))
}

class DemoActor(out: ActorRef) extends Actor {
  override def preStart() = {
    jQ("#msgform").submit { (event: JQueryEventObject) =>
      event.preventDefault()
      self ! Submit
    }
    jQ("#spinner").hide()
    jQ("#msgform").show()
  }
  
  override def postStop() = {
    jQ("#msgtext").prop("disabled", true)
    jQ("#discussion").append("<hr>")
  }

  def receive = {
    case Submit =>
      val text = jQ("#msgtext").value().toString
      if(!text.isEmpty) {
        jQ("#msgtext").value("")
        out ! Msg(text)
        Discussion.appendMy(text)
      }
      
    case Msg(text) =>
      Discussion.appendHis(text)
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
