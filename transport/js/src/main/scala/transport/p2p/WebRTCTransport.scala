package transport.p2p

import scala.concurrent._
import scala.util.{ Success, Failure }
import scala.scalajs.js

import akka.actor._

import transport._
import transport.jsapi._

import org.scalajs.spickling._
import org.scalajs.spickling.jsany._

class WebRTCTransport(implicit ec: ExecutionContext) extends Transport {
  type Address = ConnectionHandle
  
  def listen(): Future[Promise[ConnectionListener]] = 
    Future.failed(new UnsupportedOperationException("TODO"))

  def connect(signalingChannel: ConnectionHandle): Future[ConnectionHandle] = {
    new WebRTCPeer(signalingChannel, js.Math.random()).future
  }

  def shutdown(): Unit = ()
}

private class WebRTCPeer(signalingChannel: ConnectionHandle, priority: Double)(
      implicit ec: ExecutionContext) {
  import WebRTCPeer._
  registerPicklers()
  
  private val webRTCConnection = new webkitRTCPeerConnection(null, DataChannelsConstraint)
  private val connectionPromise = Promise[ConnectionHandle]()
  private var isCaller: Boolean = _

  signalingChannel.handlerPromise.success(new MessageListener {
    def notify(inboundPayload: String) = {
      val parsedPayload : js.Any = js.JSON.parse(inboundPayload)
      val unpickledPayload: Any = PicklerRegistry.unpickle(parsedPayload)
      revievedViaSignaling(unpickledPayload)
    }
    override def closed() = if(!future.isCompleted) {
      connectionPromise.failure(new IllegalStateException(
        "Signaling channel closed before the end of connection establishment."))
    }
  })

  webRTCConnection.onicecandidate = { event: RTCIceCandidateEvent =>
    if(event.candidate != null) {
      sendViaSignaling(IceCandidate(js.JSON.stringify(event.candidate)))
    }
  }
  
  sendViaSignaling(Priority(priority))
  
  def future: Future[ConnectionHandle] = connectionPromise.future

  private def sendViaSignaling(m: Any): Unit = {
    signalingChannel.write(js.JSON.stringify(PicklerRegistry.pickle(m)))
  }

  private def revievedViaSignaling(m: Any): Unit = {
    // Each message is received exactly once, in the order of appearance in this match.
    m match {
      
      case Priority(hisPriority) =>
        isCaller = hisPriority > priority
        if(isCaller) {
          createConnectionHandle(webRTCConnection.createDataChannel("sendDataChannel"))
          webRTCConnection.createOffer { description: RTCSessionDescription =>
            webRTCConnection.setLocalDescription(description)
            sendViaSignaling(SessionDescription(js.JSON.stringify(description)))
          }
        } else {
          webRTCConnection.ondatachannel = { event: Event =>
            createConnectionHandle(event.asInstanceOf[RTCDataChannelEvent].channel) // WebRTC API typo?
          }
        }
      
      case IceCandidate(candidate) =>
        webRTCConnection.addIceCandidate(new RTCIceCandidate(
          js.JSON.parse(candidate).asInstanceOf[RTCIceCandidate]))

      case SessionDescription(description) =>
        val remoteDescription = new RTCSessionDescription(
            js.JSON.parse(description).asInstanceOf[RTCSessionDescriptionInit])
        if(isCaller) {
          webRTCConnection.setRemoteDescription(remoteDescription)
        } else {
          webRTCConnection.setRemoteDescription(remoteDescription)
          webRTCConnection.createAnswer { localDescription: RTCSessionDescription =>
            webRTCConnection.setLocalDescription(localDescription)
            sendViaSignaling(SessionDescription(js.JSON.stringify(localDescription)))
          }
        }
        
    }
  }
  
  private def createConnectionHandle(dc: RTCDataChannel): Unit = {
    new ConnectionHandle {
      private val promise = Promise[MessageListener]()
      private var poorMansBuffer: Future[MessageListener] = promise.future
      
      dc.onopen = { event: Event =>
        connectionPromise.success(this)
      }
      dc.onmessage = { event: RTCMessageEvent =>
        poorMansBuffer = poorMansBuffer.andThen {
          case Success(listener) =>
            listener.notify(event.data.toString())
        }
      }
      dc.onclose = { event: Event =>
        poorMansBuffer = poorMansBuffer.andThen {
          case Success(listener) =>
            listener.closed()
        }
      }
      dc.onerror = { event: Event =>
        poorMansBuffer = poorMansBuffer.andThen {
          case Success(listener) =>
            listener.closed()
        }
      }
      
      def handlerPromise: Promise[MessageListener] = promise
      def write(outboundPayload: String): Unit = dc.send(outboundPayload)
      def close(): Unit = dc.close()
    }
  }
}
private object WebRTCPeer {
  case class Priority(value: Double)
  case class IceCandidate(string: String)
  case class SessionDescription(string: String)

  private lazy val _registerPicklers: Unit = {
    import org.scalajs.spickling._
    import PicklerRegistry.register
    
    register[Priority]
    register[IceCandidate]
    register[SessionDescription]
  }

  def registerPicklers(): Unit = _registerPicklers

  object OptionalMediaConstraint extends RTCOptionalMediaConstraint {
    override val DtlsSrtpKeyAgreement: js.Boolean = false
    override val RtpDataChannels: js.Boolean = false
  }

  object DataChannelsConstraint extends RTCMediaConstraints {
    override val mandatory: RTCMediaOfferConstraints = null
    override val optional: js.Array[RTCOptionalMediaConstraint] = js.Array(OptionalMediaConstraint)
  }
}
