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

import transport._
import transport.client._
import scala.collection.mutable

object Client extends autowire.Client[String, upickle.Reader, upickle.Writer] {
  case class IdentifiedRequest(id: Int, request: Request)
  
  var pendingPromise: Option[Promise[String]] = None

  val address = WebSocketClient.addressFromPlayTemplate
  val transport = new WebSocketClient()
  val connection = transport.connect(address)
  connection.foreach { _.handlerPromise.success(
    new MessageListener {
      def notify(inboundPayload: String) = {
        // TODO: Ain't gonna work for interleaved method calls.
        pendingPromise.foreach { _.success(inboundPayload) }
        pendingPromise = None
      }
      def closed() = ()
    }  
  )}
  
  // var promiseId: Int = 0
  // def nextPromiseId(): Int = {
  //   promiseId += 1
  //   promiseId
  // }
  // val pendingPromises = mutable.Map.empty[Integer, Promise[String]]
  
  // override def doCall(request: Request): Future[String] = {
  //   val promise: Promise[String] = Promise()
  //   val id = nextPromiseId()
  //   pendingPromises.update(id, promise)

  //   connection.foreach { _.write(upickle.write(
  //     IdentifiedRequest(id, request)
  //   ))}

  //   promise.future
  //   // dom.extensions.Ajax.post(
  //   //   url = "/api/" + req.path.mkString("/"),
  //   //   data = upickle.write(req.args)
  //   // ).map(_.responseText)
  // }
  
  override def doCall(request: Request): Future[String] = {
    connection.foreach { _.write(upickle.write(request))}
    pendingPromise = Some(Promise())
    pendingPromise.get.future
  }

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
