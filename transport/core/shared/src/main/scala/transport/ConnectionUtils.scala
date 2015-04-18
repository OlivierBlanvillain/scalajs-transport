package transport

import scala.concurrent._
import scala.util._
import scala.collection.mutable

/* Utilities for `ConnectionHandles` */
object ConnectionUtils {
  import MyPickler._

    /** Combines several connections passed in arguments and returns the resulting broadcast
     *  connection. Messages send through the broadcast are copied and sent over each of the
     *  combined connections. Messages received from any of the combined connections are
     *  forwarded to the broadcast connection. Closing the broadcast connection closes all
     *  combined connections, and the broadcast connection is closed when when any of the
     *  combined connection is closed. */
    def trivialBroadcast(cs: ConnectionHandle*)(implicit ec: ExecutionContext): ConnectionHandle = {
      val queueablePromise = QueueablePromise[MessageListener]
      val closePromise = Promise[Unit]
      cs.foreach(_.handlerPromise.success(message => queueablePromise.queue(_(message))))
      cs.foreach(_.closedFuture.foreach(_ => closePromise.trySuccess(())))
      new ConnectionHandle {
        def handlerPromise: Promise[MessageListener] = queueablePromise
        def write(message: String): Unit = cs.foreach(_.write(message))
        def closedFuture: Future[Unit] = closePromise.future
        def close(): Unit = cs.foreach(_.close())
      }
    }
  
  /** Forks a `base` connection into two, distinct connections going through `base`. The original
   *  connection has to  be forked on both sides of the connection. The returned Connections can be
   *  closed  independently, the `base` connection is closed when both connections are closed. */
  def fork(base: ConnectionHandle)(implicit ec: ExecutionContext): (ConnectionHandle, ConnectionHandle) = {
    val messageLeftPromise = QueueablePromise[MessageListener]
    val closeLeftPromise = Promise[Unit]
    val connectionLeft = new ConnectionHandle {
      def handlerPromise: Promise[MessageListener] = messageLeftPromise
      def write(message: String): Unit = base.write(MyPickler.write(Left(message)))
      def closedFuture: Future[Unit] = closeLeftPromise.future
      def close(): Unit = {
        base.write(MyPickler.write(CloseLeft))
        closeLeftPromise.trySuccess(())
      }
    }

    val messageRightPromise = QueueablePromise[MessageListener]
    val closeRightPromise = Promise[Unit]
    val connectionRight = new ConnectionHandle {
      def handlerPromise: Promise[MessageListener] = messageRightPromise
      def write(message: String): Unit = base.write(MyPickler.write(Right(message)))
      def closedFuture: Future[Unit] = closeRightPromise.future
      def close(): Unit = {
        base.write(MyPickler.write(CloseRight))
        closeRightPromise.trySuccess(())
      }
    }
    
    base.closedFuture.foreach { _ =>
      closeRightPromise.trySuccess(())
      closeLeftPromise.trySuccess(())
    }
    for {
      rightClosed <- closeRightPromise.future
      rightClosed <- closeRightPromise.future
    } base.close()
    
    base.handlerPromise.success { pickle =>
      MyPickler.read(pickle) match {
        case Left(message) => messageLeftPromise.queue(_(message))
        case Right(message) => messageRightPromise.queue(_(message))
        case CloseLeft => closeLeftPromise.trySuccess(())
        case CloseRight => closeRightPromise.trySuccess(())
      }
    }
    
    (connectionLeft, connectionRight)
  }

  /** Plugs two `ConnectionHandles` together, such that every message received by the first is
    * written in the second, and the other way around. */
  def plug(connectionLeft: ConnectionHandle, connectionRight: ConnectionHandle)(implicit ec: ExecutionContext): Unit = {
    connectionLeft.handlerPromise.success(connectionRight.write)
    connectionRight.handlerPromise.success(connectionLeft.write)
    connectionLeft.closedFuture.foreach(_ => connectionRight.close())
    connectionRight.closedFuture.foreach(_ => connectionLeft.close())
  }
  
  /** Creates a pair of connected `ConnectionHandle` objects, linked one to the other. */
  def dummyConnectionPair()(implicit ec: ExecutionContext): (ConnectionHandle, ConnectionHandle) = {
    val c1 = new ProxyConnectionHandle()
    val c2 = new ProxyConnectionHandle()
    c1.peer = c2
    c2.peer = c1
    (c1, c2)
  }
}

private object MyPickler {
  sealed trait ForkMessage
  case class Left(m: String) extends ForkMessage
  case class Right(m: String) extends ForkMessage
  case object CloseLeft extends ForkMessage
  case object CloseRight extends ForkMessage
  
  def write(message: ForkMessage): String = {
    message match {
      case Left(message) => "L" + message
      case Right(message) => "R" + message
      case CloseLeft => "C"
      case CloseRight => "D"
    }
  }
  
  def read(pickle: String): ForkMessage = {
    (pickle.head, pickle.tail) match {
      case ('L', message) => Left(message)
      case ('R', message) => Right(message)
      case ('C', _) => CloseLeft
      case ('D', _) => CloseRight
    }
  }
}

private class ProxyConnectionHandle(implicit ec: ExecutionContext) extends ConnectionHandle {
  var peer: ProxyConnectionHandle = _
  val promise = QueueablePromise[MessageListener]()
  val closePromise = Promise[Unit]()
  def notify(payload: String): Unit = promise.queue(_(payload))
    
  def closedFuture: Future[Unit] = closePromise.future
  def handlerPromise: Promise[MessageListener] = promise
  def write(outboundPayload: String): Unit = peer.notify(outboundPayload)
  def close(): Unit = peer.closePromise.success(())
}
