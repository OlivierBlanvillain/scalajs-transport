package transport.jsapi

import scala.scalajs.js

class SockJS(url: String, _reserved: js.Any, options: js.Any) extends EventTarget {
  def this(url: String) = this(url, null, null)
  def readyState: Int = ???
  def bufferedAmount: Int = ???
  var onopen: js.Function1[Event, _] = ???
  def extensions: String = ???
  var onmessage: js.Function1[MessageEvent, _] = ???
  var onclose: js.Function1[CloseEvent, _] = ???
  var onerror: js.Function1[ErrorEvent, _] = ???
  var binaryType: String = ???
  def close(code: Int = ???, reason: String = ???): Unit = ???
  def send(data: js.Any): Unit = ???
}
