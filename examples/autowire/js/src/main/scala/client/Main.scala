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
import transport.autowire._
import scala.collection.mutable
import SockJSClient.addressFromPlayRoute

object Client extends autowire.Client[String, upickle.Reader, upickle.Writer] {
  val pendingPromises = new PendingPromises[String]()
  
  val futureConnection = new SockJSClient().connect(addressFromPlayRoute())
  
  futureConnection.foreach { _.handlerPromise.success(
    new IdentifiedMessageListener(pendingPromises)
  )}
  
  def doCall(request: Request): Future[String] = futureConnection.flatMap {
    new IdentifiedCallOverConnection(_, pendingPromises)(request)
  }
  
  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}

@JSExport
object ScalaJSExample {
  @JSExport
  def main(): Unit = {
    
    Client[Api].double(21).call() onSuccess {
      case result: Int => dom.document.body.appendChild(h2(result).render)
    }
    
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
        p("Enter a file path to show"),
        inputBox,
        outputBox
      ).render
    )
  }
}
