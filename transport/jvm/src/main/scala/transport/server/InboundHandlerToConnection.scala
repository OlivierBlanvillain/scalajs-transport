package transport.server

import transport._
import scala.concurrent._

import io.netty.channel._
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.websocketx._
import io.netty.handler.codec.http.HttpHeaders.Names.HOST
import io.netty.handler.codec.http.HttpMethod._
import io.netty.util.concurrent.GenericFutureListener
import io.netty.util.concurrent.{ Future => NettyFuture }

class InboundHandlerToConnection(path: String, connectionListener: Future[ConnectionListener])(
      implicit ec: ExecutionContext) extends SimpleChannelInboundHandler[Object] {

  val queueablePromise = QueueablePromise[MessageListener]()
  var handshaker: WebSocketServerHandshaker = _

  def channelRead0(ctx: ChannelHandlerContext, msg: Object): Unit = {
    msg match {
      case f: FullHttpRequest => handleHttpRequest(ctx, f)
      case f: WebSocketFrame => handleWebSocketFrame(ctx, f)
    }
  }

  def handleHttpRequest(ctx: ChannelHandlerContext, req: FullHttpRequest): Unit = {
    if (req.getMethod() == GET && path == req.getUri()) {
      // Handshake
      val location = "ws://" + req.headers().get(HOST) + path
      val wsFactory = new WebSocketServerHandshakerFactory(location, null, true)
      handshaker = wsFactory.newHandshaker(req)
      if (handshaker == null) {
        WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel())
      } else {
        val handshakFuture = handshaker.handshake(ctx.channel(), req)
      }
    }
  }

  def handleWebSocketFrame(ctx: ChannelHandlerContext, frame: WebSocketFrame): Unit = {
    frame match {
      case closeFrame: CloseWebSocketFrame => 
        handshaker.close(ctx.channel(), closeFrame.retain())
        
      case pingFrame: PingWebSocketFrame => 
        ctx.channel().write(new PongWebSocketFrame(frame.content().retain()))
        
      case textFrame: TextWebSocketFrame =>
        val retainedText = textFrame.text()
        println(retainedText)
        queueablePromise.queue(_.notify(retainedText))
        
      case _ =>
        throw new UnsupportedOperationException(String.format(
          "%s frame types not supported", frame.getClass().getName()))
    }
  }
  
  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    val connection = new ConnectionHandle {
      def handlerPromise: Promise[MessageListener] = queueablePromise
      def write(m: String): Unit = ctx.channel().writeAndFlush(new TextWebSocketFrame(m))
      def close(): Unit = ctx.close()
    }
    connectionListener.foreach(_.notify(connection))
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    queueablePromise.queue(_.closed())
  }
  
  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    // TODO send the exception...
    ctx.close()
  }
}
