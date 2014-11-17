import ScalaJSKeys._

val commonSettings = Seq(
  organization := "org.scalajs",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.11.2",
  normalizedName ~= { _.replace("scala-js", "scalajs") },
  scalacOptions ++= Seq(
    "-language:reflectiveCalls",
    "-deprecation",
    "-unchecked",
    "-feature",
    "-encoding", "utf8"
  )
)

lazy val root = project.in(file("."))
  .settings(commonSettings: _*)
  .aggregate(transportJavascript, transportNetty, transportPlay, transportTyrus,
    transportAkkaJs, transportAkkaJvm, transportAutowireJs, transportAutowireJvm)


// Transport

val transportShared = commonSettings ++ Seq(
  unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared")

lazy val transportJavascript = project.in(file("transport/javascript"))
  .settings((transportShared ++ scalaJSSettings): _*)
  .settings(libraryDependencies ++= Seq(
    "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6",
    // TODO: These two should go away at some point.
    "org.scalajs" %%% "scalajs-pickling" % "0.3.1",
    "org.scalajs" %%% "scalajs-actors" % "0.1-SNAPSHOT"))

lazy val transportNetty = project.in(file("transport/netty"))
  .settings(transportShared: _*)
  .settings(libraryDependencies ++= Seq(
    "io.netty" % "netty-all" % "4.0.24.Final"))

lazy val transportPlay = project.in(file("transport/play"))
  .settings(transportShared: _*)
  .settings(libraryDependencies ++= Seq(
    "com.github.fdimuccio" %% "play2-sockjs" % "0.3.0",
    "com.typesafe.akka" %% "akka-actor" % "2.3.6",
    "com.typesafe.play" %% "play" % "2.3.5"))

lazy val transportTyrus = project.in(file("transport/tyrus"))
  .settings(transportShared: _*)
  .settings(libraryDependencies ++= Seq(
    "org.glassfish.tyrus.bundles" % "tyrus-standalone-client" % "1.8.3"))


val akkaShared = transportShared ++ Seq(
  unmanagedSourceDirectories in Compile += baseDirectory.value / "../akka")

lazy val transportAkkaJs = project.in(file("transport/akkajs"))
  .settings((akkaShared ++ scalaJSSettings): _*)
  .settings(libraryDependencies ++= Seq(
    "org.scalajs" %%% "scalajs-pickling" % "0.3.1",
    "org.scalajs" %%% "scalajs-actors" % "0.1-SNAPSHOT"))

lazy val transportAkkaJvm = project.in(file("transport/akkajvm"))
  .settings(akkaShared: _*)
  .settings(libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.3.6",
    "org.scalajs" %% "scalajs-pickling-play-json" % "0.3.1"))


val autowireShared = transportShared ++ Seq(
  unmanagedSourceDirectories in Compile += baseDirectory.value / "../autowire",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "upickle" % "0.2.2",
    "com.lihaoyi" %%% "autowire" % "0.2.1"))

lazy val transportAutowireJs = project.in(file("transport/autowirejs"))
  .settings((autowireShared ++ scalaJSSettings): _*)

lazy val transportAutowireJvm = project.in(file("transport/autowirejvm"))
  .settings(autowireShared: _*)


lazy val playTwoBrowsersTest = project.in(file("transport/playTwoBrowsersTest"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayScala)
  .settings(libraryDependencies ++= Seq(
    "org.seleniumhq.selenium" % "selenium-java" % "2.43.1",
    "com.github.detro.ghostdriver" % "phantomjsdriver" % "1.0.4" % "test"))
  
lazy val transportTest = project.in(file("transport/test"))
  .settings(commonSettings: _*)
  .dependsOn(transportNetty)
  .dependsOn(transportTyrus)
  .settings(libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"))


// Examples

lazy val examples = project.settings(commonSettings: _*).aggregate(
    transportTest, chatWebSocket, chatWebSocketJs, chatWebRTC, chatWebRTCJs, autowire, autowireJs)

parallelExecution in Global := false

val playWithScalaJs = Seq(
  unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared",
  unmanagedResourceDirectories in Compile += baseDirectory.value / "../js/src")

def scalaJsOfPlayProject(p: Project) = Seq(
  unmanagedSourceDirectories in Compile += (baseDirectory in p).value / "../shared",
  fastOptJS in Compile <<= (fastOptJS in Compile) triggeredBy (compile in (p, Compile)),
  crossTarget in (Compile, fastOptJS) := (baseDirectory in p).value / "public/javascripts",
  crossTarget in (Compile, fullOptJS) := (baseDirectory in p).value / "public/javascripts")


lazy val webRTCExample = project.in(file("examples/webrtc"))
  .settings((commonSettings ++ scalaJSSettings): _*)
  .dependsOn(transportJavascript, transportAkkaJs)


lazy val autowire = project.in(file("examples/autowire/jvm"))
  .enablePlugins(PlayScala)
  .settings((commonSettings ++ playWithScalaJs): _*)
  .dependsOn(transportPlay, transportAutowireJvm, playTwoBrowsersTest % "test->test")
  .settings(libraryDependencies ++= Seq(
    "org.webjars" % "sockjs-client" % "0.3.4",
    "org.webjars" %% "webjars-play" % "2.3.0",
    "org.webjars" % "bootstrap" % "3.2.0"))

lazy val autowireJs = project.in(file("examples/autowire/js"))
  .settings((commonSettings ++ scalaJSSettings ++ scalaJsOfPlayProject(autowire)): _*)
  .dependsOn(transportJavascript, transportAutowireJs)
  .settings(libraryDependencies ++= Seq(
    "com.scalatags" %%% "scalatags" % "0.4.0"))


lazy val chatWebSocket = project.in(file("examples/chat-websocket/jvm"))
  .enablePlugins(PlayScala)
  .settings((commonSettings ++ playWithScalaJs): _*)
  .dependsOn(transportPlay, transportAkkaJvm, playTwoBrowsersTest % "test->test")
  .settings(libraryDependencies ++= Seq(
    "org.webjars" %% "webjars-play" % "2.3.0",
    "org.webjars" % "sockjs-client" % "0.3.4",
    "org.webjars" % "jquery" % "2.1.1"))

lazy val chatWebSocketJs = project.in(file("examples/chat-websocket/js"))
  .settings((commonSettings ++ scalaJSSettings ++ scalaJsOfPlayProject(chatWebSocket)): _*)
  .dependsOn(transportJavascript)
  .dependsOn(transportAkkaJs)
  .settings(libraryDependencies ++= Seq(
    "org.scala-lang.modules.scalajs" %%% "scalajs-jquery" % "0.6"))


lazy val chatWebRTC = project.in(file("examples/chat-webrtc/jvm"))
  .enablePlugins(PlayScala)
  .dependsOn(transportPlay, transportAkkaJvm, playTwoBrowsersTest % "test->test")
  .settings((commonSettings ++ playWithScalaJs): _*)
  .settings(libraryDependencies ++= Seq(
    "org.webjars" %% "webjars-play" % "2.3.0",
    "org.webjars" % "sockjs-client" % "0.3.4",
    "org.webjars" % "jquery" % "2.1.1"))

lazy val chatWebRTCJs = project.in(file("examples/chat-webrtc/js"))
  .settings((commonSettings ++ scalaJSSettings ++ scalaJsOfPlayProject(chatWebRTC)): _*)
  .dependsOn(transportJavascript, transportAkkaJs)
  .settings(libraryDependencies ++= Seq(
    "org.scala-lang.modules.scalajs" %%% "scalajs-jquery" % "0.6"))
