package transport.javascript

import scala.concurrent._

import scala.util._
import scala.scalajs.js

import transport._
import transport.jsapi._

/** TODOC */
class WebRTCSignalingFallback(implicit ec: ExecutionContext) extends Transport {
  import WebRTCSignalingFallback._
  
  type Address = ConnectionHandle
  
  def listen(): Future[Promise[ConnectionListener]] = 
    Future.failed(new UnsupportedOperationException(
      "WebRTCSignalingFallback cannot listen for incomming connections."))

  def connect(signalingChannel: ConnectionHandle): Future[ConnectionHandle] = {
    val connectionPromise = Promise[ConnectionHandle]()
    val queueablePromise = QueueablePromise[MessageListener]()
    val forwarderConnection = new ConnectionHandle {
      def handlerPromise: Promise[MessageListener] = queueablePromise
      def closedFuture: Future[Unit] = signalingChannel.closedFuture
      def write(outboundPayload: String): Unit = signalingChannel.write(outboundPayload)
      def close(): Unit = signalingChannel.close()
    }

    val supports = supportsWebRTC()
    var first = true
    signalingChannel.write(js.JSON.stringify(supports))
    signalingChannel.handlerPromise.success { string =>
      if(first) {
        first = false
        val remoteSupports = js.JSON.parse(string).asInstanceOf[Boolean]
        if(supports && remoteSupports) {
          connectionPromise.completeWith(new WebRTCPeer(forwarderConnection).future)
        } else {
          connectionPromise.success(forwarderConnection)
        }
      } else {
        queueablePromise.queue(_(string))
      }
    }
    
    connectionPromise.future
  }

  def shutdown(): Future[Unit] = Future.successful(Unit)
}

object WebRTCSignalingFallback {
  /** Chrome only ATM. */
  def supportsWebRTC(): Boolean = {
    Try(new webkitRTCPeerConnection(null).iceConnectionState).isSuccess
  }
}
