package controllers

import play.api.mvc._
import play.api.libs.concurrent.Akka.system
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import actors._
import models._
import transport._
import transport.server._
import transport.akka._

object Application extends Controller {
  RegisterPicklers.registerPicklers()
  
  val peerMatcher = system.actorOf(PeerMatcher.props, "PeerMatcher")

  def indexDev = Action { implicit request =>
    Ok(views.html.index(devMode = true, transport.server.SockJSServer.javascriptRoute(sockJS)))
  }

  def indexOpt = Action { implicit request =>
    Ok(views.html.index(devMode = false, transport.server.SockJSServer.javascriptRoute(sockJS)))
  }
  
  val webSocketTransport = WebSocketServer()
  val webSocket = webSocketTransport.action()

  val sockJStransport = SockJSServer()
  val sockJS = sockJStransport.action()
  
  AcceptWithActor(UserActor.props(peerMatcher))(sockJStransport)
}
