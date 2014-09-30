package transport

import ConnectionHandle._

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

import scala.collection.mutable.Queue

object Proxy {
  def apply(): (ConnectionHandle, ConnectionHandle) = {
    ???
  }
}

class ProxyConnectionHandle {
  
  private var peer: ProxyConnectionHandle = _
  private val promise: Promise[MessageListener] = Promise()
  private val pendingMessages: Queue[Message] = new Queue[Message]
  
  private sealed trait Message
  private case object Closed extends Message
  private case class Payload(payload: String) extends Message
  
  // promise.future.onSuccess {
    // pendingMessages.foreach {
    //   case Payload =>
    //   case Closed =>
    // }
  // }
  
  private def notify(outboundPayload: String): Unit = {
    promise.future.value match {
      case Some(Success(peer)) =>
        peer.notify(outboundPayload)
      case _ =>
        pendingMessages.enqueue(Payload(outboundPayload))
    }
  }
  
  private def notifyClosed(): Unit = {
    promise.future.value match {
      case Some(Success(peer)) =>
        peer.closed()
      case _ =>
        pendingMessages.enqueue(Closed)
    }
  }
  
  def handlerPromise: Promise[MessageListener] = promise
  def write(outboundPayload: String): Unit = peer.notify(outboundPayload)
  def close(): Unit = peer.notifyClosed()
  
}


case class ProxyId(id: Int)
case class ProxyMessage(id: Int, message: String)
