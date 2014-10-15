package transport

import transport._

case class WebSocketUrl(url: String)

trait WebSocketTransport extends Transport {
  type Address = WebSocketUrl
}
