package transport.akka

import akka.actor._

import transport._

import scala.concurrent._

case class ActorWrapper[T <: Transport](transport: T)(implicit ec: ExecutionContext, system: ActorSystem) {
  
  def connectWithActor(address: transport.Address)(handlerProps: ActorRef => Props) {
    transport.connect(address).foreach { connection =>
      system.actorOf(ConnectionToActor.props(connection, handlerProps))
    }
  }

  def acceptWithActor(handlerProps: ActorRef => Props) {
    transport.listen().map { promise =>
      promise.success(new ConnectionListener {
        def notify(inboundConnection: ConnectionHandle): Unit = {
          system.actorOf(ConnectionToActor.props(inboundConnection, handlerProps))
        }
      })
    }
  }
}
