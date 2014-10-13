package transport

import scala.concurrent._

/** SPI for asynchronous transport mechanisms. */
trait Transport {
  /** Abstract representation of a mean sufficient to establish a connection between two
   *  Transports. */
  type Address
  
  /** Asynchronously attempts to listen and accept incoming connection. In case of successful
   *  attempt, returns a Promise of a ConnectionListener. By completing this Promise, thelistener
   *  becomes responsible for handling incoming connection. While the Promise is not completed no
   *  incoming connections are accepted. */
  def listen(): Future[Promise[ConnectionListener]]
  
  /** Asynchronously opens a duplex connection between two Transports. */
  def connect(remote: Address): Future[ConnectionHandle]
  
  /** Shuts down the Transport and releases all corresponding resources. */
  def shutdown(): Unit

}

/** An interface to be implemented by the user of Transport to listen to inbound connections. */
trait ConnectionListener {

  /** Called by the Transport to notify an inbound ConnectionHandle. */
  def notify(inboundConnection: ConnectionHandle): Unit
}

/** SPI for duplex connections created by a Transports. */
trait ConnectionHandle {
  /** Returns a Promise to be completed to listen for incoming payload. Incoming messages should be
   *  buffered until the listener is registered. */
  def handlerPromise: Promise[MessageListener]

  /** Asynchronously sends a payload to the remote endpoint. */
  def write(outboundPayload: String): Unit

  /** Closes connection. */
  def close(): Unit

}

/** An interface to be implemented by the user of a ConnectionHandle to listen to inbound
 *  payloads. */
trait MessageListener {
  
  /** Called by the ConnectionHandle to notify an inbound payload. */
  def notify(inboundPayload: String): Unit
  
  /** Called by the ConnectionHandle when the connection is closed. */
  def closed(): Unit
}
