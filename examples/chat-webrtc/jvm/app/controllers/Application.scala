package controllers

import play.api.mvc._
import play.api.libs.concurrent.Akka.system
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import actors._

import transport.ConnectionUtils
import transport.play._
import transport.akka._

object Application extends Controller {
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
  
  webSocketTransport.listen().foreach { _.success { connection =>
    val (left, right) = ConnectionUtils.fork(connection)
    system.actorOf(ConnectionToActor.props(left, UserActor.props(peerMatcher, right)))
  }}
}
