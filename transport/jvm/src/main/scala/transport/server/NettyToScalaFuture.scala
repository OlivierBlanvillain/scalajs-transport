package transport.server

import io.netty.channel.{ Channel, ChannelFuture, ChannelFutureListener }
import scala.concurrent.{ Future, Promise }

object NettyToScalaFuture {
  
  implicit class F(channelFuture: ChannelFuture) {
    
    def toScala: Future[Channel] = {
      val promise = Promise[Channel]()
      channelFuture.addListener(new ChannelFutureListener: Unit {
        def operationComplete(cf: ChannelFuture) = {
          if (cf.isSuccess)
            promise.success(cf.channel)
          else if (cf.isCancelled)
            promise.failure(new RuntimeException("Cancelled"))
          else
            promise.failure(cf.cause)
        }
      })
      promise.future
    }

  }
}
