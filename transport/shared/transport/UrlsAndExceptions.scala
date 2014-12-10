package transport

case class SockJSUrl(url: String)
// TODO: Error Failed to construct 'WebSocket': The URL's scheme must be either 'ws' or 'wss'. 'wws' is not allowed.
case class WebSocketUrl(url: String)

case class SockJSException(message: String) extends Exception
case class WebSocketException(message: String) extends Exception
case class WebRTCException(message: String) extends Exception

trait SockJSTransport extends Transport {
  type Address = SockJSUrl
}

trait WebSocketTransport extends Transport {
  type Address = WebSocketUrl
}
