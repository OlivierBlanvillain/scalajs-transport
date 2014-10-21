package transport.akka

import akka.actor._
import akka.scalajs.jsapi._

import scala.scalajs.js

import org.scalajs.spickling._
import org.scalajs.spickling.jsany._

import transport._
import scala.concurrent._

case class HandleWithActor(handlerProps: ActorRef => Props)(fc: Future[ConnectionHandle])(
      implicit ec: ExecutionContext, system: ActorSystem) {
  
  fc.foreach { connection =>
    system.actorOf(Props(new ConnectionProxy(connection, handlerProps)))
  }
}

private class ConnectionProxy(connection: ConnectionHandle, handlerProps: ActorRef => Props)
    extends AbstractProxy(handlerProps) {
  import AbstractProxy._
  
  type PickleType = js.Any
  implicit protected def pickleBuilder: PBuilder[PickleType] = JSPBuilder
  implicit protected def pickleReader: PReader[PickleType] = JSPReader
  
  override def preStart(): Unit = {
    super.preStart()
    self ! ConnectionOpened
    
    connection.handlerPromise.success {
      new MessageListener {
        override def notify(inboundPayload: String): Unit = self ! js.JSON.parse(inboundPayload)
        override def closed(): Unit = self ! ConnectionClosed
      }
    }
  }

  override def postStop() = {
    super.postStop()
    connection.close()
  }

  override protected def sendPickleToPeer(pickle: PickleType): Unit = {
    connection.write(js.JSON.stringify(pickle))
  }
}
