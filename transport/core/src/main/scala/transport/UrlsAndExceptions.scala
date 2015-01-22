package transport

case class SockJSUrl(url: String) {
  val prefix = url.takeWhile(_ != '/')
  if(prefix != "http:" && prefix != "https:") {
    throw new IllegalArgumentException(s"""The URL's scheme must be either "ws:" or "wss:". "$prefix" is not allowed.""")
  }
}

case class WebSocketUrl(url: String) {
  val prefix = url.takeWhile(_ != '/')
  if(prefix != "ws:" && prefix != "wss:") {
    throw new IllegalArgumentException(s"""The URL's scheme must be either "ws:" or "wss:". "$prefix" is not allowed.""")
  }
}

case class SockJSException(message: String) extends Exception
case class WebSocketException(message: String) extends Exception
case class WebRTCException(message: String) extends Exception

trait SockJSTransport extends Transport {
  type Address = SockJSUrl
}

trait WebSocketTransport extends Transport {
  type Address = WebSocketUrl
}
