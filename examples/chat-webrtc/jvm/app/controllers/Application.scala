package controllers

import play.api.mvc._
import play.api.libs.concurrent.Akka.system
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import actors._
import models._

import transport.play._
import transport.akka._

object Application extends Controller {
  RegisterPicklers.registerPicklers()
  
  implicit val implicitSystem = system
  
  val peerMatcher = system.actorOf(PeerMatcher.props)

  def indexDev = Action { implicit request =>
    Ok(views.html.index(devMode = true, PlayUtils.webSocketRoute(routes.Application.webSocket)))
  }

  def indexOpt = Action { implicit request =>
    Ok(views.html.index(devMode = false, PlayUtils.webSocketRoute(routes.Application.webSocket)))
  }
  
  val webSocketTransport = new WebSocketServer()
  val webSocket = webSocketTransport.action()
  
  ActorWrapper(webSocketTransport).acceptWithActor(UserActor.props(peerMatcher))
}
