// package transport

// import scala.concurrent._
// import scala.concurrent.ExecutionContext.Implicits.global
// import scala.util.{ Failure, Success }

// case class RelayTransport(underlingTransport: Transport) extends Transport {
//   import Transport._
//   import ConnectionHandle._
  
//   trait Info
//   trait RelayAddress
//   case class ServerAddress(server: underlingTransport.Address) extends RelayAddress
//   case class PeerAddress(server: underlingTransport.Address, info: Info) extends RelayAddress
  
//   override type Address = RelayAddress
  
//   private val connectionListenerPromise: Promise[ConnectionListener] = Promise()
  
//   override def listen(): Future[(Address, Promise[ConnectionListener])] = {
//     underlingTransport.listen.map {
//       case (address, promise) =>
//         connectionListenerPromise.future.onComplete {
//           case Success(connectionListener: ConnectionListener) =>
//             // TODO: Magic! Read either Regular or Promise message to determine what to do.
//             val cl = new ConnectionListener {
//               def notify(inboundConnection: ConnectionHandle): Unit = {
//                 inboundConnection.handlerPromise.success {
//                   new MessageListener {
//                     var first: Boolean = true
//                     def notify(inboundPayload: String): Unit = {
//                       if(first) {
//                         first = false
//                         inboundPayload match {
//                           case "a" =>
                            
//                           case "b" =>
                            
//                         }
//                       } else {
                        
//                       }
//                     }
//                     def closed(): Unit = ()
//                   }
//                 }
//               }
//             }
//             promise.success(cl)
//           case Failure(e) =>
//             promise.failure(e)
//         }
//         (ServerAddress(address), connectionListenerPromise)
//     }
//   }

//   def route(info: Info): Option[ConnectionListener] = ???
  
//   override def connect(remote: Address): Future[ConnectionHandle] = ???
  
//   override def shutdown(): Future[Unit] = underlingTransport.shutdown()
// }
