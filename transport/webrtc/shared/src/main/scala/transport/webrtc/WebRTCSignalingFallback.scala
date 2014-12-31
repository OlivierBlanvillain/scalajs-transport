package transport.webrtc

import scala.concurrent._
import transport._

/** TODOC */
class WebRTCSignalingFallback(implicit ec: ExecutionContext) extends Transport {
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

    val supports = TestFeatureSupport.webRTC()
    var first = true
    signalingChannel.write(if(supports) "T" else "F")
    signalingChannel.handlerPromise.success { string =>
      if(first) {
        first = false
        val remoteSupports = string == "T"
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
