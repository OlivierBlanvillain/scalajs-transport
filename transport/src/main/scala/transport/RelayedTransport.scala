package transport

import scala.concurrent._
import scala.util.{ Failure, Success }
import Transport._

sealed trait RelayedAddress
case object LocalAdresse extends RelayedAddress
case class RelayedAdresse(id: Long) extends RelayedAddress

object RelayedTransport extends Transport {
  
  type Address = RelayedAddress
    
  private val connectionListenerPromise: Promise[ConnectionListener] = Promise()
  
  override def listen(): Future[(Address, Promise[ConnectionListener])] =
    Future.successful((LocalAdresse, connectionListenerPromise))
  
  override def connect(remote: Address): Future[ConnectionHandle] = {
    remote match {
      
      case LocalAdresse =>
        connectionListenerPromise.future.value match {
          case Some(Success(connectionListener)) =>
            val (connectionHandle1, connectionHandle2) = connectedHandlersPair() 
            connectionListener.notify(connectionHandle1)
            Future.successful(connectionHandle2)
          case Some(Failure(e)) =>
            Future.failed(e)
          case _ =>
            Future.failed(new Exception("TODO"))
        }
        
      case RelayedAdresse(id) => 
        val relayConnection = magicMap(id)
        // relayConnection.write(connect)
        ???
      
    }
  }
  
  private def connectedHandlersPair(): (ConnectionHandle, ConnectionHandle) = ???
  private def magicMap(id: Long): Transport = ???
  
  override def shutdown(): Future[Unit] = ???
  
}

// uPickle and scala-js-pickling RelayedAddress serializers that do the "magic"
