package object transport {
  /** Listener provided by the user of Transport to listen to inbound connections. */
  type ConnectionListener = ConnectionHandle => Unit

  /** Listener provided by the user of a ConnectionHandle to listen to inbound payloads. */
  type MessageListener = String => Unit

  case class SockJSUrl(url: String)
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
}
