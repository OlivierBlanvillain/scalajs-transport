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

    val PORT = 8080

    val bossGroup = new NioEventLoopGroup(1)
    val workerGroup = new NioEventLoopGroup()
    try {
      val b = new ServerBootstrap()
      b.group(bossGroup, workerGroup)
       .channel(classOf[NioServerSocketChannel])
       .childHandler(WebSocketServerInitializer)

      val ch = b.bind(PORT).sync().channel()

      println(
        "Open your web browser and navigate to http://127.0.0.1:" + PORT + '/')

      ch.closeFuture().sync()
    } finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
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

    val WEBSOCKET_PATH = "/websocket"

    var handshaker: WebSocketServerHandshaker = _

    def channelRead0(ctx: ChannelHandlerContext, msg: Object): Unit = {
      if (msg.isInstanceOf[FullHttpRequest]) {
        handleHttpRequest(ctx, msg.asInstanceOf[FullHttpRequest])
      } else if (msg.isInstanceOf[WebSocketFrame]) {
        handleWebSocketFrame(ctx, msg.asInstanceOf[WebSocketFrame])
      }
    }

    override def channelReadComplete(ctx: ChannelHandlerContext) {
      ctx.flush()
    }

    def handleHttpRequest(ctx: ChannelHandlerContext, req: FullHttpRequest): Unit = {
      if (!req.getDecoderResult().isSuccess()) {
        sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST))
        return
      }

      // Allow only GET methods.
      if (req.getMethod() != GET) {
        sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN))
        return
      }

      // Send the demo page and favicon.ico
      if ("/".equals(req.getUri())) {
        val content: ByteBuf = ??? // WebSocketServerIndexPage.getContent(getWebSocketLocation(req))
        val res = new DefaultFullHttpResponse(HTTP_1_1, OK, content)

        res.headers().set(CONTENT_TYPE, "text/html charset=UTF-8")
        HttpHeaders.setContentLength(res, content.readableBytes())

        sendHttpResponse(ctx, req, res)
        return
      }
      if ("/favicon.ico".equals(req.getUri())) {
        val res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND)
        sendHttpResponse(ctx, req, res)
        return
      }

      // Handshake
      val wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, true)
      handshaker = wsFactory.newHandshaker(req)
      if (handshaker == null) {
        WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel())
      } else {
        handshaker.handshake(ctx.channel(), req)
      }
    }

    def handleWebSocketFrame(ctx: ChannelHandlerContext, frame: WebSocketFrame): Unit = {

        // Check for closing frame
        if (frame.isInstanceOf[CloseWebSocketFrame]) {
          handshaker.close(ctx.channel(), frame.retain().asInstanceOf[CloseWebSocketFrame])
          return
        }
        if (frame.isInstanceOf[PingWebSocketFrame]) {
          ctx.channel().write(new PongWebSocketFrame(frame.content().retain()))
          return
        }
        if (!(frame.isInstanceOf[TextWebSocketFrame])) {
          throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()))
        }

        // Send the uppercase string back.
        val request = (frame.asInstanceOf[TextWebSocketFrame]).text()
        System.err.printf("%s received %s%n", ctx.channel(), request)
        ctx.channel().write(new TextWebSocketFrame(request.toUpperCase()))
    }

    def sendHttpResponse(ctx: ChannelHandlerContext, req: FullHttpRequest, res: FullHttpResponse): Unit = {
      // Generate an error page if response getStatus code is not OK (200).
      if (res.getStatus().code() != 200) {
        val buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8)
        res.content().writeBytes(buf)
        buf.release()
        HttpHeaders.setContentLength(res, res.content().readableBytes())
      }

      // Send the response and close the connection if necessary.
      val f = ctx.channel().writeAndFlush(res)
      if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
        f.addListener(ChannelFutureListener.CLOSE)
      }
    }

    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
      cause.printStackTrace()
      ctx.close()
    }

    def getWebSocketLocation(req: FullHttpRequest): String = {
      "ws://" + req.headers().get(HOST) + WEBSOCKET_PATH
    }
}
