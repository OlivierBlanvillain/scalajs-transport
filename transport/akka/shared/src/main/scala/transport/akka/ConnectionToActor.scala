package transport.akka

import akka.actor._
import transport._
import scala.concurrent._

// TODoc
class ConnectionToActor(
      connection: ConnectionHandle,
      handlerProps: ActorRef => Props)(
      implicit ec: ExecutionContext)
    extends AbstractProxy(handlerProps) with Serializer {
  import AbstractProxy._
  
  override def preStart(): Unit = {
    super.preStart()
    self ! ConnectionOpened
    
    connection.handlerPromise.success { inboundPayload =>
      self ! parse(inboundPayload)
    }
    
    connection.closedFuture.onComplete { _ =>
      self ! ConnectionClosed
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
  def props(connection: ConnectionHandle, handlerProps: ActorRef => Props)(implicit ec: ExecutionContext) = Props(new ConnectionToActor(connection, handlerProps))
}
