package client

import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import scala.util.Random
import scala.concurrent._
import scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scalatags.JsDom.all._
import upickle._
import autowire._
import shared.Api

import transport.client._
import transport.util._
import scala.collection.mutable
import SockJSClient.addressFromPlayRoute

object Client extends autowire.Client[String, upickle.Reader, upickle.Writer] {
  val connection = new SockJSClient().connect(addressFromPlayRoute())
  val ccc = connectionSomethingClient(connection)
  def doCall(request: Request): Future[String] = ccc.doCall(request)
  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}

@JSExport
object ScalaJSExample {
  @JSExport
  def main(): Unit = {
    
    val inputBox = input.render
    val outputBox = div.render

    def updateOutput() = {
      Client[Api].list(inputBox.value).call().foreach { paths =>
        outputBox.innerHTML = ""
        outputBox.appendChild(
          ul(
            for(path <- paths) yield {
              li(path)
            }
          ).render
        )
      }
    }
    inputBox.onkeyup = {(e: dom.Event) =>
      updateOutput()
    }
    updateOutput()
    dom.document.body.appendChild(
      div(
        cls:="container",
        h1("File Browser"),
        p("Enter a file path to s"),
        inputBox,
        outputBox
      ).render
    )
  }
}
