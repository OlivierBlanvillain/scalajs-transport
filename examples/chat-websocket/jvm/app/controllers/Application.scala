package controllers

import play.api.mvc._
import play.api.libs.concurrent.Akka.system
import play.api.Play.current

import akka.scalajs.server.SockJSServer

import actors._
import models._

object Application extends Controller {
  RegisterPicklers.registerPicklers()
  
  val peerMatcher = system.actorOf(PeerMatcher.props, "PeerMatcher")

  def indexDev = Action { implicit request =>
    Ok(views.html.index(devMode = true, transport.server.SockJSServer.javascriptRoute(sockjs)))
  }

  def indexOpt = Action { implicit request =>
    Ok(views.html.index(devMode = false, transport.server.SockJSServer.javascriptRoute(sockjs)))
  }

  lazy val sockjs = SockJSServer.acceptWithActor { out =>
    UserActor.props(peerMatcher, out)
  }
}
