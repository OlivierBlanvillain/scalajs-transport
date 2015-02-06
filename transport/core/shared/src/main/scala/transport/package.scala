package object transport {

  /** Listener provided by the user of Transport to listen to inbound connections. */
  type ConnectionListener = ConnectionHandle => Unit

  /** Listener provided by the user of a ConnectionHandle to listen to inbound payloads. */
  type MessageListener = String => Unit

}
