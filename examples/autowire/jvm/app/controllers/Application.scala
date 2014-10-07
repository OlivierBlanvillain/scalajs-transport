package controllers

import scala.concurrent._
import scala.util.Success

import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import play.api.Play.current
import akka.actor._

import upickle._
import shared.Api
import autowire.Core.Request

import transport._
import websocket._
import transport.websocket.WebSocketPlayServer

object Application extends Controller {
  
  def indexDev = Action {
    Ok(views.html.index(devMode = true))
  }

  def indexOpt = Action {
    Ok(views.html.index(devMode = false))
  }
  
  def socket = transport.action()
  
  val transport = WebSocketPlayServer(routes.Application.socket)
  
  transport.listen().map {
    _._2.success {
      new ConnectionListener {
        override def notify(connection: ConnectionHandle): Unit = {
          connection.handlerPromise.success {
            new MessageListener {
              override def notify(pickle: String): Unit = {
                val request: Request[String] = upickle.read[Request[String]](pickle)
                val result: Future[String] = AutowireServer.route[Api](Server)(request)
                result.foreach { connection write _ }
              }
              override def closed(): Unit = ()
            }
          }
        }
      }
    }
  }
}

// class MyWebSocketActor(out: ActorRef) extends Actor {
//   override def receive = {
//     case pickle: String =>
//       val request: Request[String] = upickle.read[Request[String]](pickle)
//       val result: Future[String] = AutowireServer.route[Api](Server)(request)
//       result.foreach { out ! _ }
//   }
// }
// object MyWebSocketActor {
//   def props(out: ActorRef) = Props(new MyWebSocketActor(out))
// }

object Server extends Api {
  def list(path: String): Seq[String] = {
    val chunks = path.split("/", -1)
    val prefix = "./" + chunks.dropRight(1).mkString("/")
    val files = Option(new java.io.File(prefix).list()).toSeq.flatten
    files.filter(_.startsWith(chunks.last))
  }
}

object AutowireServer extends autowire.Server[String, upickle.Reader, upickle.Writer]{
  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}
