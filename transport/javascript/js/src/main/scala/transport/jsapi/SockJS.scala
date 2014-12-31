package transport.jsapi

import scala.scalajs.js
import org.scalajs.dom._

class SockJS(url: String, _reserved: js.Any, options: js.Any) extends EventTarget {
  def this(url: String) = this(url, null, null)
  def readyState: Int = js.native
  def bufferedAmount: Int = js.native
  var onopen: js.Function1[Event, _] = js.native
  def extensions: String = js.native
  var onmessage: js.Function1[MessageEvent, _] = js.native
  var onclose: js.Function1[CloseEvent, _] = js.native
  var onerror: js.Function1[ErrorEvent, _] = js.native
  var binaryType: String = js.native
  def close(code: Int = js.native, reason: String = js.native): Unit = js.native
  def send(data: js.Any): Unit = js.native
}
