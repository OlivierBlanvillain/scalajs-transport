package transport

import scala.concurrent._
import scala.util.{ Failure, Success }

import scala.collection.mutable

private class ProxyConnectionHandle(implicit ec: ExecutionContext) extends ConnectionHandle {
  private var peer: ProxyConnectionHandle = _
  private val promise: QueueablePromise[MessageListener] = QueueablePromise()
  private def closed(): Unit = promise.queue(_.closed())
  private def notify(payload: String): Unit = promise.queue(_.notify(payload))
    
  def handlerPromise: Promise[MessageListener] = promise
  def write(outboundPayload: String): Unit = peer.notify(outboundPayload)
  def close(): Unit = peer.closed()
}

/** TODOC */
object ProxyConnectionHandle {
  
  /** TODOC */
  def newConnectionsPair()(implicit ec: ExecutionContext): (ConnectionHandle, ConnectionHandle) = {
    val c1 = new ProxyConnectionHandle()
    val c2 = new ProxyConnectionHandle()
    c1.peer = c2
    c2.peer = c1
    (c1, c2)
  }
}
