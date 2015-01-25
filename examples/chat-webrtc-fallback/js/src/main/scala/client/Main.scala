package client

import scala.scalajs.js.annotation.JSExport
import scala.concurrent.Future
import scala.scalajs.js
import org.scalajs.jquery.{ jQuery => jQ, _ }

import akka.actor._

import transport._
import transport.webrtc._
import transport.javascript._
import transport.akka._
import transport.play.PlayUtils.webSocketFromPlayRoute

import scalajs.concurrent.JSExecutionContext.Implicits.runNow

@JSExport("Client")
object Main {
  implicit val system = ActorSystem("chat-client")

  @JSExport
  def startup(): Unit = {
    jQ("body").append(
      if(TestFeatureSupport.webRTC())
        "<div>Supports WebRTC</div>"
      else
        "<div>Does not support WebRTC</div>"
    )

    new WebSocketClient().connect(webSocketFromPlayRoute()).foreach { connection =>
      val (left, right) = ConnectionUtils.fork(connection)
      system.actorOf(ConnectionToActor.props(left, EstablishRtcActor.props(right)))
    }
  }
}

class EstablishRtcActor(connection: ConnectionHandle, out: ActorRef) extends Actor {
  def receive = {
    case "Plugged" =>
      import Main.system
      ActorWrapper(new WebRTCClientFallback()).connectWithActor(connection)(DemoActor.props)
  }
}
object EstablishRtcActor {
  def props(connection: ConnectionHandle)(out: ActorRef) =
    Props(new EstablishRtcActor(connection, out))
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
        out ! text
        Discussion.appendMy(text)
      }
      
    case text: String =>
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
