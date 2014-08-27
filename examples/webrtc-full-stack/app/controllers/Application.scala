package controllers

import scala.language.postfixOps

import scala.concurrent.duration._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent.Akka

import play.sockjs.api._
import akka.actor._
import akka.pattern.ask
import akka.scalajs.wsserver.ActorWebSocket
import actors._

object Application extends Controller {

  import play.api.Play.current

  implicit val timeout = akka.util.Timeout(5 seconds)
  implicit def ec = Akka.system.dispatcher

  // val peerMatcher = Akka.system.actorOf(Props[PeerMatcher], name = "peermatcher")
  val board = Akka.system.actorOf(Props[BoardActor], name = "board")

  def indexDev = Action {
    Ok(views.html.index(devMode = true))
  }

  def indexOpt = Action {
    Ok(views.html.index(devMode = false))
  }

  def chatWSEntry = WebSocket.acceptWithActor[String, String] { request => 
    UserActor.props(board)
  }
}
