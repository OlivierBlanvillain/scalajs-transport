package transport.play

import scala.concurrent._

import play.api.Application
import play.sockjs.api._

import transport._

/** SockJS server based on the [[https://github.com/fdimuccio/play2-sockjs play2-sockjs]] plugin.
 *  
 *  Incoming connections have to be passed to SockJSServer using its .action() method.
 *
 *  For example,
 *  {{{
 *  // In conf/routes:
 *  ->      /socket                     controllers.Application.socket
 *  
 *  // In controllers.Application:
 *  val transport = SockJSServer()
 *  val socket = transport.action()
 *  }}}
 *  
 *  When using Play, the dependence to the client side sockjs.js can be fulfilled using WebJars.
 *  In your build.sbt:
 *  {{{
 *  libraryDependencies ++= Seq(
 *    "org.webjars" %% "webjars-play" % "2.3.0",
 *    "org.webjars" % "sockjs-client" % "0.3.4")
 *  }}}
 *  
 *  In the play routes:
 *  {{{
 *  GET     /webjars&#47;*file              controllers.WebJarAssets.at(file)
 *  }}}
 *  
 *  And in your views:
 *  {{{
 *  <script type='text/javascript' src='@routes.WebJarAssets.at(WebJarAssets.locate("sockjs.min.js"))'></script>
 *  }}}
 *  
 */
class SockJSServer(implicit ec: ExecutionContext, app: Application)
    extends SockJSTransport {
  private val promise = Promise[ConnectionListener]()
  
  /** Method to be called from a play controller for each new SockJS connection. */
  def action(): SockJSRouter = SockJSRouter.tryAcceptWithActor[String, String] {
    BridgeActor.actionHandle(promise)
  }
  
  def listen(): Future[Promise[ConnectionListener]] =
    Future.successful(promise)
  
  def connect(remote: SockJSUrl): Future[ConnectionHandle] =
    Future.failed(new UnsupportedOperationException(
      "Servers cannot initiate SockJSs connections."))
  
  def shutdown(): Future[Unit] = Future.successful(Unit)
}
