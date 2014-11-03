package transport.server

import transport._
import scala.concurrent._

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.websocketx._
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.logging.LogLevel
import io.netty.util.CharsetUtil
import io.netty.handler.codec.http.HttpHeaders.Names._
import io.netty.handler.codec.http.HttpMethod._
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.handler.codec.http.HttpVersion._

class WebSocketServer extends App {
  def run: Unit = {
    
  val PORT = 8080
  val bossGroup = new NioEventLoopGroup(1)
  val workerGroup = new NioEventLoopGroup()
  try {
    val b = new ServerBootstrap()
    b.group(bossGroup, workerGroup)
     .channel(classOf[NioServerSocketChannel])
     .childHandler(WebSocketServerInitializer)

    val ch = b.bind(PORT).sync().channel()

    println("Open your web browser and navigate to http://127.0.0.1:" + PORT + '/')

    ch.closeFuture().sync()
  } finally {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
  }
}

object WebSocketServerInitializer extends ChannelInitializer[SocketChannel] {
  override def initChannel(ch: SocketChannel): Unit = {
    val pipeline = ch.pipeline()
    pipeline.addLast(new HttpServerCodec())
    pipeline.addLast(new HttpObjectAggregator(65536))
    pipeline.addLast(new WebSocketServerHandler())
  }
}


class WebSocketServerHandler extends SimpleChannelInboundHandler[Object] {
  def channelRead0(ctx: ChannelHandlerContext, msg: Object): Unit = {
    msg match {
      case f: FullHttpRequest => handleHttpRequest(ctx, f)
      case f: WebSocketFrame => handleWebSocketFrame(ctx, f)
    }
  }

  def handleHttpRequest(ctx: ChannelHandlerContext, req: FullHttpRequest): Unit = {
    if (!req.getDecoderResult().isSuccess()) {
      sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST))
    } else if (req.getMethod() != GET || !"/".equals(req.getUri()))
      sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN))
    } else {
      val location = "ws://" + req.headers().get(HOST)
      val wsFactory = new WebSocketServerHandshakerFactory(location, null, true)
      handshaker = wsFactory.newHandshaker(req)
      if (handshaker == null) {
        WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel())
      } else {
        handshaker.handshake(ctx.channel(), req)
      }
    }
    
    // Serve HTML:
    // val content: ByteBuf = Unpooled.copiedBuffer("Hello HTML!", CharsetUtil.US_ASCII)
    // val res = new DefaultFullHttpResponse(HTTP_1_1, OK, content)

    // res.headers().set(CONTENT_TYPE, "text/html charset=UTF-8")
    // HttpHeaders.setContentLength(res, content.readableBytes())

    // sendHttpResponse(ctx, req, res)
  }

  def handleWebSocketFrame(ctx: ChannelHandlerContext, frame: WebSocketFrame): Unit = {
    frame match {
      case _: CloseWebSocketFrame => 
        handshaker.close(ctx.channel(), frame.retain().asInstanceOf[CloseWebSocketFrame])
        
      case _: PingWebSocketFrame => 
        ctx.channel().write(new PongWebSocketFrame(frame.content().retain()))
        
      case f: TextWebSocketFrame => 
        // Send the uppercase string back.
        val request = f.text()
        System.err.printf("%s received %s%n", ctx.channel(), request)
        ctx.channel().write(new TextWebSocketFrame(request.toUpperCase()))
        
      case _ =>
        throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()))
    }
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    ctx.close()
  }
}
