package example

import scala.concurrent._
import transport._
import transport.webrtc.{ WebRTCClientFallback => WebRTCClient }
import transport.tyrus._
import scala.concurrent.ExecutionContext.Implicits.global

object WebRTCExample { /* */
val relayURL: WebSocketUrl = ??? /* */

val webSocketClient = new WebSocketClient()
val webRTCClient = new WebRTCClient()
  
val signalingChannel: Future[ConnectionHandle] =
  webSocketClient.connect(relayURL)
  
val p2pConnection: Future[ConnectionHandle] =
  signalingChannel.flatMap(webRTCClient.connect(_))
  
} /* */
