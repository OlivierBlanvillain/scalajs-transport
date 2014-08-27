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
import org.scalajs.jquery.{jQuery => jQ, _}
import akka.scalajs.jsapi._

@JSExport("Client")
object Main {
  // RegisterPicklers.registerPicklers()
  val system = ActorSystem("chat-client")

  @JSExport
  def startup(): Unit = {
    println("Starting up...")
    system.actorOf(WebSocketClientProxy.props("ws://localhost:9000/websocket")(DemoActor.props()))
  }
}

class DemoActor(out: ActorRef) extends Actor {
  override def preStart() = {
    jQ("#msgform").submit({ (event: JQueryEventObject) =>
      event.preventDefault()
      val message = jQ("#msgtext").value()
      jQ("#msgtext").value("")
      out ! message
    })
  }
  
  def receive = {
    case message => 
      val discussion = jQ("#discussion")
      discussion.append(
        s"""<li class="self">
           |  <div class="avatar"></div>
           |  <div class="messages">
           |    <p>$message</p>
           |  </div>
           |</li>
        """.stripMargin)
      discussion.scrollTop(discussion.prop("scrollHeight").asInstanceOf[Int]) 
  }
}
object DemoActor {
  def props()(out: ActorRef) = Props(new DemoActor(out))
}

case class Msg(s: String)

class WebSocketClientProxy(url: String, handlerProps: ActorRef => Props) extends Actor {
  var webSocket: WebSocket = _

  override def preStart() = {
    val handlerActor = context.watch(context.actorOf(handlerProps(self)))
    
    webSocket = new WebSocket(url)
    webSocket.addEventListener("message", { (event: Event) =>
      handlerActor ! js.JSON.parse(event.asInstanceOf[MessageEvent].data.toString())
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
      webSocket.send(js.JSON.stringify(message.asInstanceOf[js.Any]))
  }
}
object WebSocketClientProxy {
  def props(url: String)(handlerProps: ActorRef => Props) =
    Props(new WebSocketClientProxy(url, handlerProps))
}


// case object AttemptToConnect
// case class Send(text: String)
// case class CreatePrivateChatRoom(dest: User)
// case class AcceptPrivateChatWith(peerUser: User, peer: ActorRef)
// case object Disconnect
// case object Disconnected

// class Manager extends Actor {
//   val proxyManager = context.actorOf(Props(new ProxyManager))

//   private[this] var myStatusAlert: JQuery = jQ()
//   def statusAlert: JQuery = myStatusAlert
//   def statusAlert_=(v: JQuery): Unit = {
//     myStatusAlert.remove()
//     myStatusAlert = v
//     myStatusAlert.prependTo(Main.notifications)
//   }

//   def makeStatusAlert(style: String): JQuery =
//     jQ(s"""<div class="alert alert-$style">""")

//   def receive = disconnected()

//   def disconnected(nextReconnectTimeout: FiniteDuration = 1 seconds): Receive = LoggingReceive {
//     case m @ AttemptToConnect =>
//       proxyManager ! m
//       statusAlert = makeStatusAlert("info").append(
//         jQ("<strong>Connecting ...</strong>"),
//         jQ("<span> </span>").append(
//           jQ("""<a href="#">""").text("Cancel").click {
//             (e: JQueryEventObject) =>
//               self ! Disconnect
//               false
//           }
//         )
//       )
//       context.setReceiveTimeout(5 seconds)
//       context.become(connecting(nextReconnectTimeout))
//   }

//   def waitingToAutoReconnect(autoReconnectDeadline: FiniteDuration,
//       nextReconnectTimeout: FiniteDuration): Receive = LoggingReceive {
//     case m @ AttemptToConnect =>
//       context.become(disconnected(nextReconnectTimeout))
//       self ! m

//     case m @ ReceiveTimeout =>
//       val now = nowDuration
//       if (now >= autoReconnectDeadline)
//         self ! AttemptToConnect
//       else {
//         val remaining = autoReconnectDeadline - nowDuration
//         jQ(".reconnect-remaining-seconds").text(remaining.toSeconds.toString)
//       }
//   }

//   def connecting(nextReconnectTimeout: FiniteDuration): Receive = LoggingReceive { withDisconnected(nextReconnectTimeout) {
//     case ReceiveTimeout | Disconnect =>
//       context.setReceiveTimeout(Duration.Undefined)
//       proxyManager ! Disconnect

//     case m @ WebSocketConnected(entryPoint) =>
//       context.setReceiveTimeout(Duration.Undefined)

//       val service = entryPoint
//       service ! Connect(null)
//       val alert = makeStatusAlert("success").append(
//         jQ("<strong>").text(s"Connected as {Main.me.nick}")
//       )
//       statusAlert = alert
//       Timers.setTimeout(3000) {
//         alert.fadeOut()
//       }

//       // for ((room, tab) <- Main.roomTabInfos)
//       //   self ! Join(room)
//       // for ((peerUser, tab) <- Main.privateChatTabInfos)
//       //   self ! CreatePrivateChatRoom(peerUser)

//       context.become(connected(service))
//   } }

//   def connected(service: ActorRef): Receive = LoggingReceive { withDisconnected() {
//     case m @ Disconnect =>
//       proxyManager ! m
//   } }

//   def withDisconnected(nextReconnectTimeout: FiniteDuration = 1 seconds)(
//       receive: Receive): Receive = receive.orElse[Any, Unit] {
//     case Disconnected =>
//       context.children.filterNot(proxyManager == _).foreach(context.stop(_))
//       statusAlert = makeStatusAlert("danger").append(
//         jQ("""<strong>You have been disconnected from the server.</strong>"""),
//         jQ("""<span>Will try to reconnect in """+
//             """<span class="reconnect-remaining-seconds"></span>"""+
//             """ seconds. </span>""").append(
//           jQ("""<a href="#">""").text("Reconnect now").click {
//             (e: JQueryEventObject) =>
//               self ! AttemptToConnect
//           }
//         )
//       )
//       jQ(".reconnect-remaining-seconds").text(nextReconnectTimeout.toSeconds.toString)
//       val autoReconnectDeadline = nowDuration + nextReconnectTimeout
//       context.setReceiveTimeout(1 seconds)
//       context.become(waitingToAutoReconnect(
//           autoReconnectDeadline, nextReconnectTimeout*2))
//   }

//   private def nowDuration = System.currentTimeMillis() milliseconds
// }

// class ProxyManager extends Actor {
//   def receive = {
//     case AttemptToConnect =>
//       context.watch(context.actorOf(
//         Props(new WebSocketClientProxy("ws://localhost:9000/websocket", context.parent))))

//     case Disconnect =>
//       context.children.foreach(context.stop(_))

//     case Terminated(proxy) =>
//       context.children.foreach(context.stop(_))
//       context.parent ! Disconnected
//   }
// }
