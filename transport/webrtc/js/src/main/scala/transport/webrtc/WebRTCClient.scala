package transport.webrtc

import scala.concurrent._
import scala.util._
import scala.scalajs.js

import transport._
import transport.jsapi._

/** WebRTC JavaScript client. (Chrome and Firefox only) 
 *  
 *  Usage example:
 *  {{{
 *  new WebRTCClient().connect(signalingChannel).foreach { connection =>
 *    connection.handlerPromise.success { string => println("Recived: " + string) }
 *    connection.write("Hello WebRTC!")
 *  }
 *  }}}
 */
class WebRTCClient(implicit ec: ExecutionContext) extends Transport {
  type Address = ConnectionHandle
  
  def listen(): Future[Promise[ConnectionListener]] = 
    Future.failed(new UnsupportedOperationException(
      "WebRTCClient cannot listen for incomming connections."))

  def connect(signalingChannel: ConnectionHandle): Future[ConnectionHandle] = {
    new WebRTCPeer(signalingChannel).future
  }

  def shutdown(): Future[Unit] = Future.successful(Unit)
}

private class WebRTCPeer(
      signalingChannel: ConnectionHandle,
      priority: Double=Random.nextDouble)(
      implicit ec: ExecutionContext) {

  import WebRTCPeer._
  
  private val webRTCConnection = new webkitRTCPeerConnection(null, null)
  private val connectionPromise = Promise[ConnectionHandle]()
  private var isCaller: Boolean = _

  signalingChannel.handlerPromise.success { inboundPayload  =>
    revievedViaSignaling(unpickle(inboundPayload))
  }
  
  signalingChannel.closedFuture.onComplete { _ =>
    connectionPromise.tryFailure(new IllegalStateException(
      "Signaling channel closed before the end of connection establishment."))
  }

  webRTCConnection.onicecandidate = { event: RTCIceCandidateEvent =>
    if(event.candidate != null) {
      sendViaSignaling(IceCandidate(event.candidate))
    }
  }
  
  sendViaSignaling(Priority(priority))
  
  def future: Future[ConnectionHandle] = connectionPromise.future

  private def sendViaSignaling(m: WebRTCMessage): Unit = {
    signalingChannel.write(pickle(m))
  }

  private def revievedViaSignaling(m: WebRTCMessage): Unit = {
    // Each message is received exactly once, in the order of appearance in this match.
    m match {
      case Priority(hisPriority) if hisPriority > priority =>
        isCaller = true
        createConnectionHandle(webRTCConnection.createDataChannel("sendDataChannel"))
        webRTCConnection.createOffer { description: RTCSessionDescription =>
          webRTCConnection.setLocalDescription(description)
          sendViaSignaling(SessionDescription(description))
        }

      case Priority(hisPriority) =>
        isCaller = false
        webRTCConnection.ondatachannel = { event: Event =>
          // WebRTC API typo?
          createConnectionHandle(event.asInstanceOf[RTCDataChannelEvent].channel)
        }
      
      case IceCandidate(candidate) =>
        webRTCConnection.addIceCandidate(candidate)

      case SessionDescription(remoteDescription) if isCaller =>
        webRTCConnection.setRemoteDescription(remoteDescription)

      case SessionDescription(remoteDescription) =>
        webRTCConnection.setRemoteDescription(remoteDescription)
        webRTCConnection.createAnswer { localDescription: RTCSessionDescription =>
          webRTCConnection.setLocalDescription(localDescription)
          sendViaSignaling(SessionDescription(localDescription))
        }
    }
  }
  
  private def createConnectionHandle(dc: RTCDataChannel): Unit = {
    new ConnectionHandle {
      private val promise = QueueablePromise[MessageListener]()
      private val closePromise = Promise[Unit]()
      
      dc.onopen = { event: Event =>
        connectionPromise.success(this)
      }
      dc.onmessage = { event: RTCMessageEvent =>
        promise.queue(_(event.data.toString))
      }
      dc.onclose = { event: Event =>
        closePromise.trySuccess(())
      }
      dc.onerror = { event: Event =>
        val message = try { event.toString } catch { case e: ClassCastException => "" }
        closePromise.tryFailure(WebRTCException(message))
      }
      
      def handlerPromise: Promise[MessageListener] = promise
      def closedFuture: Future[Unit] = closePromise.future
      def write(outboundPayload: String): Unit = dc.send(outboundPayload)
      def close(): Unit = dc.close()
    }
  }
}

private object WebRTCPeer {
  sealed trait WebRTCMessage
  case class Priority(value: Double) extends WebRTCMessage
  case class IceCandidate(candidate: RTCIceCandidate) extends WebRTCMessage
  case class SessionDescription(description: RTCSessionDescription) extends WebRTCMessage
  
  def pickle(message: WebRTCMessage): String = {
    message match {
      case Priority(value: Double) =>
        "P" + value.toString
      case IceCandidate(candidate: RTCIceCandidate) =>
        "I" + js.JSON.stringify(candidate)
      case SessionDescription(description: RTCSessionDescription) =>
        "S" + js.JSON.stringify(description)
    }
  }
  
  def unpickle(pickle: String): WebRTCMessage = {
    (pickle.head, pickle.tail) match {
      case ('P', value) =>
        Priority(value.toDouble)
      case ('I', string) =>
        IceCandidate(new RTCIceCandidate(
          js.JSON.parse(string).asInstanceOf[RTCIceCandidate]))
      case ('S', string) =>
        SessionDescription(new RTCSessionDescription(
          js.JSON.parse(string).asInstanceOf[RTCSessionDescriptionInit]))
    }
  }
}
