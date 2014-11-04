package transport.server

import transport._
import scala.concurrent._

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http._
import io.netty.util.concurrent.GenericFutureListener
import io.netty.util.concurrent.{ Future => NettyFuture }

class WebSocketServer(port: Int, path: String)(implicit ec: ExecutionContext)
     extends WebSocketTransport {
  val bossGroup = new NioEventLoopGroup(1)
  val workerGroup = new NioEventLoopGroup()
  
  def listen(): Future[Promise[ConnectionListener]] = {
    val connectionListenerPromise = Promise[ConnectionListener]()
    val channelInitializer = new ChannelInitializer[SocketChannel] {
      override def initChannel(ch: SocketChannel): Unit = {
        val pipeline = ch.pipeline()
        pipeline.addLast(new HttpServerCodec())
        pipeline.addLast(new HttpObjectAggregator(65536))
        pipeline.addLast(new InboundHandlerToConnection(path, connectionListenerPromise.future))
      }
    }
    
    val b = new ServerBootstrap()
    b.group(bossGroup, workerGroup)
     .channel(classOf[NioServerSocketChannel])
     .childHandler(channelInitializer)

    val bindFuture = b.bind(port)
    val bindPromise = Promise[Promise[ConnectionListener]]()
    bindFuture.addListener(new GenericFutureListener[NettyFuture[Void]] {
      def operationComplete(f: NettyFuture[Void]): Unit = {
        println(s"Serving WebSocket on ws://localhost:$port$path ...")
        bindPromise.success(connectionListenerPromise)
      }
    })
    bindPromise.future
  }
  
  def connect(remote: Address): Future[ConnectionHandle] = 
    Future.failed(new UnsupportedOperationException("TODO"))

  def shutdown(): Unit = {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
    bossGroup.shutdown()
    workerGroup.shutdown()
  }
}
