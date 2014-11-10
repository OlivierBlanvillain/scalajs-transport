package transport.akka

import akka.actor._

import transport._

import scala.concurrent._

/** TODOC */
class ActorWrapper[T <: Transport](transport: T)(implicit ec: ExecutionContext, sys: ActorSystem) {
  
  /** TODOC */
  def connectWithActor(address: transport.Address)(handlerProps: ActorRef => Props) {
    transport.connect(address).foreach { connection =>
      sys.actorOf(ConnectionToActor.props(connection, handlerProps))
    }
  }

  /** TODOC */
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
