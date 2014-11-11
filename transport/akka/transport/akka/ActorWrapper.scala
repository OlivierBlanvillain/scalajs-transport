package transport.akka

import akka.actor._

import transport._

import scala.concurrent._

/** Wraps a Transport to offer actor interfaces. The semantic is as follows: each connection
 *  corresponds a single handler actor which receives data from the connections as messages, and
 *  can send data thought the connection by sending messages to the ActorRef provided in its
 *  constructor. The life time of this handler actor is coupled with the life time of the
 *  connection. As a consequence, detecting when the connection is opened or closed, can be done by
 *  respectively overriding the preStart() and the postStop() methods. Closing the connection can
 *  be achieved by sending a PoisonPill to the handler actor. */
class ActorWrapper[T <: Transport](transport: T)(implicit ec: ExecutionContext, sys: ActorSystem) {
  
  /** Connect to an address with by creating an actor with the given props.
   *
   *  For example:
   *  {{{
   *  myTransport.connectWithActor(address) { out =>
   *    myHandlerActor.props(out)
   *  }
   *  }}} */
  def connectWithActor(address: transport.Address)(handlerProps: ActorRef => Props) {
    transport.connect(address).foreach { connection =>
      sys.actorOf(ConnectionToActor.props(connection, handlerProps))
    }
  }

  /** Start listening for incoming connections which will be accepted using the given props.
   *
   *  Given an actor ref to send messages to, the function passed should return the props for an
   *  actor to create to handle a new connection.
   *
   *  For example:
   *  {{{
   *  myTransport.acceptWithActor { out =>
   *    myHandlerActor.props(out)
   *  }
   *  }}} */
  def acceptWithActor(handlerProps: ActorRef => Props) {
    transport.listen().map { promise =>
      promise.success(new ConnectionListener {
        def notify(inboundConnection: ConnectionHandle): Unit = {
          sys.actorOf(ConnectionToActor.props(inboundConnection, handlerProps))
        }
      })
    }
  }
  
}

object ActorWrapper {
  def apply[T <: Transport](transport: T)(implicit ec: ExecutionContext, sys: ActorSystem) =
    new ActorWrapper(transport)
}
