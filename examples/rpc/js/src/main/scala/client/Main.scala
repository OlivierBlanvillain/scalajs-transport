package client

import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import scala.util.Random
import scala.concurrent._
import scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scalatags.JsDom.all._
import shared.Api

import transport.javascript._
import transport.rpc._
import autowire._
import upickle._

import scala.collection.mutable
import SockJSClient.addressFromPlayRoute

@JSExport
object ScalaJSExample {
  @JSExport
  def main(): Unit = {
    
    val transport = new SockJSClient()
    val address = addressFromPlayRoute()
    val Client = new RpcWrapper(transport).connect(address)
    
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
