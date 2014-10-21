package transport.akka

import akka.actor._

import org.scalajs.spickling._

import transport._
import scala.concurrent._

case class ActorWrapper[T <: Transport](transport: T)(implicit ec: ExecutionContext, system: ActorSystem) {
  
  def connectWithActor(address: transport.Address)(handlerProps: ActorRef => Props) {
    transport.connect(address).foreach { connection =>
      system.actorOf(Props(new ConnectionProxyxyxyxy(connection, handlerProps)))
    }
  }

  def acceptWithActor(handlerProps: ActorRef => Props) {
    transport.listen().map { promise =>
      promise.success(new ConnectionListener {
        def notify(inboundConnection: ConnectionHandle): Unit = {
          system.actorOf(Props(new ConnectionProxyxyxyxy(inboundConnection, handlerProps)))
        }
      })
    }
  }
}

private class ConnectionProxyxyxyxy(connection: ConnectionHandle, handlerProps: ActorRef => Props)
      extends AbstractProxy(handlerProps) with Serializer {
  import AbstractProxy._
  
  override def preStart(): Unit = {
    super.preStart()
    self ! ConnectionOpened
    
    connection.handlerPromise.success {
      new MessageListener {
        override def notify(inboundPayload: String): Unit = self ! parse(inboundPayload)
        override def closed(): Unit = self ! ConnectionClosed
      }
    }
  }

  override def postStop() = {
    super.postStop()
    connection.close()
  }

  override protected def sendPickleToPeer(pickle: PickleType): Unit = {
    connection.write(stringify(pickle))
  }
}

trait AbstractSerializer {
  type PickleType
  implicit protected def pickleBuilder: PBuilder[PickleType]
  implicit protected def pickleReader: PReader[PickleType]
  def parse(s: String): PickleType
  def stringify(p: PickleType): String
}
