package transport

import transport._
import scala.concurrent._

case class WebSocketUrl(url: String)

trait WebSocketTransport extends Transport {
  type Address = WebSocketUrl
}

case class SockJSUrl(url: String)

trait SockJSTransport extends Transport {
  type Address = SockJSUrl
}

// trait SockJSTransport extends Transport
// trait SockJSClient extends SockJSTransport
// trait SockJSServer extends SockJSTransport
// trait WebRTCTransport extends Transport
