package transport

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }

import scala.collection.mutable

class ProxyConnectionHandle extends ConnectionHandle {
  import ProxyConnectionHandle._
  
  private var peer: ProxyConnectionHandle = _
  private val promise: Promise[MessageListener] = Promise()
  private val pendingMessages: mutable.Queue[Message] = new mutable.Queue[Message]
  
  promise.future.onSuccess {
    case listener =>
      pendingMessages.foreach { incoming(_, listener) }
  }
  
  private def incoming(message: Message, listener: MessageListener): Unit = {
    message match {
      case Closed =>
        listener.closed()
      case Payload(payload) =>
        listener.notify(payload)
    }
  }
  
  private def notify(message: Message): Unit = {
    promise.future.value match {
      case Some(Success(listener)) =>
        incoming(message, listener)
      case _ =>
        pendingMessages.enqueue(message)
    }
  }
    
  def handlerPromise: Promise[MessageListener] = promise
  def write(outboundPayload: String): Unit = peer.notify(Payload(outboundPayload))
  def close(): Unit = peer.notify(Closed.asInstanceOf[Message])
  
}
object ProxyConnectionHandle {
  private sealed trait Message
  private case object Closed extends Message
  private case class Payload(payload: String) extends Message
  
  def newPair(): (ConnectionHandle, ConnectionHandle) = {
    val c1 = new ProxyConnectionHandle()
    val c2 = new ProxyConnectionHandle()
    c1.peer = c2
    c2.peer = c1
    (c1, c2)
  }
}


case class ProxyId(id: Int)
case class ProxyMessage(id: Int, message: String)
