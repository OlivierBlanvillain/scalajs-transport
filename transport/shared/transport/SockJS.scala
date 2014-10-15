package transport

import transport._

case class SockJSUrl(url: String)

trait SockJSTransport extends Transport {
  type Address = SockJSUrl
}
