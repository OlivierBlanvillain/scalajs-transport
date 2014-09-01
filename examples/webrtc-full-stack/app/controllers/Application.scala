package controllers

import scala.language.postfixOps

import scala.concurrent.duration._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent.Akka.system
import play.api.Play.current

import play.sockjs.api._
import akka.actor._
import akka.pattern.ask
import akka.scalajs.wsserver.ActorWebSocket
import actors._
import org.scalajs.spickling._
import org.scalajs.spickling.playjson._

import models._

object Application extends Controller {
  RegisterPicklers.registerPicklers()
  
  val peerMatcher = system.actorOf(PeerMatcher.props)

  def indexDev = Action {
    Ok(views.html.index(devMode = true))
  }

  def indexOpt = Action {
    Ok(views.html.index(devMode = false))
  }

  def chatWSEntry = WebSocketServer.acceptWithActor { out =>
    UserActor.props(peerMatcher, out)
  }
}

object WebSocketServer {
  def acceptWithActor(handlerProps: ActorRef => Props) = {
    WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
      Props(new WebSocketServerProxy(out, handlerProps))
    }
  }
}

class WebSocketServerProxy(out: ActorRef, handlerProps: ActorRef => Props) extends Actor {
  
  var handlerActor: ActorRef = _
  
  override def preStart() = {
    val pickleAndForward = context.watch(context.actorOf(Props(new PickleAndForward(out))))
    this.handlerActor = context.watch(context.actorOf(handlerProps(pickleAndForward)))
  }

  def receive = {
    case Terminated(_) =>
      context.stop(self)
    case pickle =>
      handlerActor ! PicklerRegistry.unpickle(pickle.asInstanceOf[JsValue])
  }
}

class PickleAndForward(out: ActorRef) extends Actor {
  def receive = {
    case message =>
      out ! PicklerRegistry.pickle(message)
  }
}
