package transport

import scala.concurrent._

/** SPI for asynchronous transport mechanisms. */
trait Transport {
  /** Abstract representation of a mean sufficient to establish a connection between two
   *  Transports. */
  type Address
  
  /** Asynchronously attempts to listen and accept incoming connection. In case of successful
   *  attempt, returns a Promise of a ConnectionListener. By completing this Promise, the listener
   *  becomes responsible for handling incoming connection. While the Promise is not completed no
   *  incoming connections are accepted. */
  def listen(): Future[Promise[ConnectionListener]]
  
  /** Asynchronously opens a duplex connection between two Transports. */
  def connect(remote: Address): Future[ConnectionHandle]
  
  /** Shuts down the Transport and releases all corresponding resources. */
  def shutdown(): Future[Unit]
}

/** SPI for duplex connections created by a Transports. */
trait ConnectionHandle {
  /** Returns a Promise to be completed to listen for incoming messages. Incoming messages are
   *  buffered until the listener is registered. */
  def handlerPromise: Promise[MessageListener]
  
  /** ConnectionHandle asynchronous signals the end connection by completing the closedFuture. */
  def closedFuture: Future[Unit]
  
  /** Asynchronously sends a message to the remote endpoint. */
  def write(message: String): Unit
  
  /** Closes connection. */
  def close(): Unit
}
