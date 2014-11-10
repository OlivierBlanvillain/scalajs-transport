package transport.akka

import akka.actor._
import transport._
import scala.concurrent._

private class ConnectionToActor(connection: ConnectionHandle, handlerProps: ActorRef => Props)
      extends AbstractProxy(handlerProps) with Serializer {
  import AbstractProxy._
  
  override def preStart(): Unit = {
    super.preStart()
    self ! ConnectionOpened
    
    connection.handlerPromise.success {
      new MessageListener {
        def notify(inboundPayload: String): Unit = self ! parse(inboundPayload)
        override def closed(): Unit = self ! ConnectionClosed
      }
    }
  }

  override def postStop(): Unit = {
    super.postStop()
    connection.close()
  }

  override protected def sendPickleToPeer(pickle: PickleType): Unit = {
    connection.write(stringify(pickle))
  }
}
object ConnectionToActor {
  def props(connection: ConnectionHandle, handlerProps: ActorRef => Props) =
    Props(new ConnectionToActor(connection, handlerProps))
}
