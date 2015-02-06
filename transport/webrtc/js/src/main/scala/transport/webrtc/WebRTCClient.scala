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
  import TestFeatureSupport.mozRTC

  private val webRTCConnection =
    if(mozRTC) new mozRTCPeerConnection(null, null) else new webkitRTCPeerConnection(null, null)
  private val connectionPromise = Promise[ConnectionHandle]()
  private var isCaller: Boolean = _

  signalingChannel.handlerPromise.success { inboundPayload  =>
    revievedViaSignaling(unpickle(inboundPayload))
  }
  
  signalingChannel.closedFuture.onComplete { _ =>
    connectionPromise.tryFailure(new IllegalStateException(
      "Signaling channel closed before the end of connection establishment."))
  }
  
  connectionPromise.future.onComplete { _ =>
    signalingChannel.close()
  }

  webRTCConnection.onicecandidate = { event: RTCIceCandidateEvent =>
    if(event.candidate != null) {
      sendViaSignaling(IceCandidate(event.candidate))
    }
  }
  
  sendViaSignaling(Priority(priority))
  
  def future: Future[ConnectionHandle] = connectionPromise.future

  private def sendViaSignaling(m: WebRTCMessage): Unit = {
    println("Sending: " + m.toString)
    signalingChannel.write(pickle(m))
  }

  private def revievedViaSignaling(m: WebRTCMessage): Unit = {
    // Each message is received exactly once, in the order of appearance in this match.
    println(m.toString)
    m match {
      case Priority(hisPriority) if hisPriority > priority =>
        isCaller = true
        createConnectionHandle(webRTCConnection.createDataChannel("sendDataChannel"))
        webRTCConnection.createOffer({ description: RTCSessionDescription =>
          webRTCConnection.setLocalDescription(description)
          sendViaSignaling(SessionDescription(description))
        }, { err: Any => () })

      case Priority(hisPriority) =>
        isCaller = false
        webRTCConnection.ondatachannel = { event: Event =>
          // WebRTC API typo?
          createConnectionHandle(event.asInstanceOf[RTCDataChannelEvent].channel)
        }
      
      case IceCandidate(candidate) =>
        webRTCConnection.addIceCandidate(candidate)

      case SessionDescription(remoteDescription) if isCaller =>
        println("Set remote  decr" + remoteDescription)
        webRTCConnection.setRemoteDescription(remoteDescription)

      case SessionDescription(remoteDescription) =>
        println("Try to createAnswer")
        webRTCConnection.setRemoteDescription(remoteDescription)
        webRTCConnection.createAnswer({ localDescription: RTCSessionDescription =>
          println("Created Answer, set local descr" + localDescription)
          webRTCConnection.setLocalDescription(localDescription)
          sendViaSignaling(SessionDescription(localDescription))
        }, { err: Any => {println("failed to Create Answer"); System.err.println(err.toString)} })
    }
  }
  
  private def createConnectionHandle(dc: RTCDataChannel): Unit = {
    new ConnectionHandle {
      private val promise = QueueablePromise[String => Unit]()
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
      
      def handlerPromise: Promise[String => Unit] = promise
      def closedFuture: Future[Unit] = closePromise.future
      def write(outboundPayload: String): Unit = dc.send(outboundPayload)
      def close(): Unit = dc.close()
    }
  }
}

private object WebRTCPeer {
  import TestFeatureSupport.mozRTC
  
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
        IceCandidate(
          if(mozRTC) new mozRTCIceCandidate(js.JSON.parse(string).asInstanceOf[mozRTCIceCandidate])
          else new RTCIceCandidate(js.JSON.parse(string).asInstanceOf[RTCIceCandidate])
        )
      case ('S', string) =>
        val init = js.JSON.parse(string).asInstanceOf[RTCSessionDescriptionInit]
        SessionDescription(
          if(mozRTC) new mozRTCSessionDescription(init)
          else new RTCSessionDescription(init)
        )
    }
  }
}
