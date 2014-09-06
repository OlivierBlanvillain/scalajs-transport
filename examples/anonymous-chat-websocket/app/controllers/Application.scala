package controllers

import play.api.mvc._
import play.api.libs.concurrent.Akka.system
import play.api.Play.current

import akka.scalajs.server.WebSocketServer

import actors._
import models._

object Application extends Controller {
  RegisterPicklers.registerPicklers()
  
  val peerMatcher = system.actorOf(PeerMatcher.props, "PeerMatcher")

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
