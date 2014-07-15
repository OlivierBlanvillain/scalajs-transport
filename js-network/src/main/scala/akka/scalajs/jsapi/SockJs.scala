package akka.scalajs.jsapi

import scala.scalajs.js

class SockJS(url: js.String, _reserved: js.Any, options: js.Any) extends js.Object with EventTarget {
  def this(url: js.String) = this(url, null, null)
  def send(message: js.String): Unit = ???
  def close(code: js.Number, reason: js.String): Unit = ???
  def close(code: js.Number): Unit = ???
  def close(): Unit = ???
}
