package transport.netty

import transport._
import transport.netty.NettyToScalaFuture._
import scala.concurrent.{ Future, Promise, ExecutionContext }

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.SocketChannel
import io.netty.channel.group.DefaultChannelGroup
import io.netty.handler.codec.http._
import io.netty.util.concurrent.{ GlobalEventExecutor, GenericFutureListener }

/** Netty WebSocket server.
 *  
 *  Usage example:
 *  {{{
 *  val capsLockEchoServer = new WebSocketServer(8080, "/websocket")
 *  
 *  capsLockEchoServer.listen().foreach(_.success { inboundConnection =>
 *    connection.handlerPromise.success { message =>
 *      connection.write(message.toUpperCase)
 *    }
 *  })
 *  
 *  println("Press enter to interrupt")
 *  System.in.read()
 *  
 *  capsLockEchoServer.shutdown()
 *  }}}
 */
class WebSocketServer(port: Int, path: String)(implicit ec: ExecutionContext)
     extends WebSocketTransport {
  private val bossGroup = new NioEventLoopGroup(1)
  private val workerGroup = new NioEventLoopGroup()
  private val allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
         
  def listen(): Future[Promise[ConnectionListener]] = {
    val listenerPromise = Promise[ConnectionListener]()
    
    val channelInitializer = new ChannelInitializer[SocketChannel] {
      override def initChannel(ch: SocketChannel): Unit = {
        val pipeline = ch.pipeline()
        pipeline.addLast(new HttpServerCodec())
        pipeline.addLast(new HttpObjectAggregator(65536))
        pipeline.addLast(new InboundHandlerToConnection(path, listenerPromise.future, allChannels))
      }
    }
    
    val b = new ServerBootstrap()
    
    b.group(bossGroup, workerGroup)
     .channel(classOf[NioServerSocketChannel])
     .childHandler(channelInitializer)

    b.bind(port).toScala.map { channel =>
      allChannels.add(channel)
      println(s"Serving WebSocket on ws://localhost:$port$path ...")
      listenerPromise
    }
  }
  
  def connect(remote: Address): Future[ConnectionHandle] = 
    Future.failed(new UnsupportedOperationException(
      "Netty WebSocketServer cannot act as a client. Use tyrus.WebSocketClient instead."))

  def shutdown(): Unit = {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
    allChannels.close().awaitUninterruptibly()
  }
}
