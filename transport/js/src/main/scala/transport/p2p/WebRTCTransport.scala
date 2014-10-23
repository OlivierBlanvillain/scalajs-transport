package transport.p2p

import scala.concurrent._

import akka.actor._

import transport._

class WebRTCTransport extends Transport {
  type Address = ActorRef
  
  def listen(): Future[Promise[ConnectionListener]] = ???
  
  def connect(remote: ActorRef): Future[ConnectionHandle] = {
    val connectionPromise = Promise[ConnectionHandle]()
    
    new ConnectionHandle {
      val promise = Promise[MessageListener]()
      private var poorMansBuffer: Future[MessageListener] = promise.future
      
            // listener.notify(event.asInstanceOf[MessageEvent].data.toString())
            // listener.closed()
      override def handlerPromise: Promise[MessageListener] = promise
      
      override def write(outboundPayload: String): Unit = ???
      
      override def close(): Unit = ???
    }

    connectionPromise.future
  }
  
  def shutdown(): Unit = ()
}

case class IceCandidate(string: String)
case class SessionDescription(string: String)
case class SignalingChannel(peer: ActorRef)
