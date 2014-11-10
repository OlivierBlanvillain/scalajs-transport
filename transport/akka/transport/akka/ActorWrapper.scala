package transport.akka

import akka.actor._

import transport._

import scala.concurrent._

/** TODOC */
class ActorWrapper[T <: Transport](transport: T)(implicit ec: ExecutionContext, sys: ActorSystem) {
  
  /** Connect to an address with by creating an actor with the given props.
   *
   *  For example:
   *  {{{
   *  myTransport.connectWithActor(address) { out =>
   *    myHandlerActor.props(out)
   *  }
   *  }}}
   */
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
   *  }}}
   */
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
