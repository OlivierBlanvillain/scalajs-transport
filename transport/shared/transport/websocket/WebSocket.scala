package transport.websocket

import transport._
import scala.concurrent._

case class WebSocketUrl(url: String)

trait WebSocketTransport extends Transport {
  type Address = WebSocketUrl
}

// trait SockJSTransport extends Transport
// trait SockJSClient extends SockJSTransport
// trait SockJSServer extends SockJSTransport
// trait WebRTCTransport extends Transport
