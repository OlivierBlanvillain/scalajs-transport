package controllers

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

import upickle._
import shared.Api

object Application extends Controller {
  
  def indexDev = Action {
    Ok(views.html.index(devMode = true))
  }

  def indexOpt = Action {
    Ok(views.html.index(devMode = false))
  }

  def api(segments: String) = Action.async { request =>
    val body: String = request.body.asText.getOrElse("")
    AutowireServer.route[Api](Server)(
      autowire.Core.Request(segments.split("/"), upickle.read[Map[String, String]](body))
    ).map(Ok(_))
  }
}

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
