val commonSettings = Seq(
  organization := "org.scalajs",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.11.4",
  normalizedName ~= { _.replace("scala-js", "scalajs") },
  scalacOptions ++= Seq(
    "-deprecation",           
    "-encoding", "UTF-8",
    "-feature",                
    "-unchecked",
    "-language:reflectiveCalls",
    "-Yno-adapted-args",       
    "-Ywarn-numeric-widen",   
    "-Xfuture",
    "-Xlint"
  )
)

parallelExecution in Global := false

lazy val root = project.in(file("."))
  .settings(commonSettings: _*)
  .aggregate(transportJavascript, transportNetty, transportPlay, transportTyrus,
    transportAkkaJs, transportAkkaJvm, transportRpcJs, transportRpcJvm)

lazy val examples = project.settings(commonSettings: _*).aggregate(
  transportTest, chatWebSocket, chatWebRTC, chatWebRTCFallback, rpc)


// lazy val transportCore = crossProject.
//   settings(commonSettings: _*).
//   jvmSettings().
//   jsSettings(libraryDependencies ++= Seq(
//     "org.scala-js" %%% "scalajs-dom" % "0.7.0",
//     // TODO: These two should go away at some point.
//     "org.scalajs" %%% "scalajs-pickling" % "0.4-SNAPSHOT",
//     "org.scalajs" %%% "scalajs-actors" % "0.1-SNAPSHOT"))

// lazy val transportCoreJVM = transportCore.jvm
// lazy val transportCoreJS = transportCore.js

// Transport

val transportShared = commonSettings ++ Seq(
  unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared")

lazy val transportJavascript = project.in(file("transport/javascript"))
  .settings(transportShared: _*)
  .enablePlugins(ScalaJSPlugin)
  .settings(libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.7.0",
    // TODO: These two should go away at some point.
    "org.scalajs" %%% "scalajs-pickling" % "0.4-SNAPSHOT",
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
  .settings(akkaShared: _*)
  .enablePlugins(ScalaJSPlugin)
  .settings(libraryDependencies ++= Seq(
    "org.scalajs" %%% "scalajs-pickling" % "0.4-SNAPSHOT",
    "org.scalajs" %%% "scalajs-actors" % "0.1-SNAPSHOT"))

lazy val transportAkkaJvm = project.in(file("transport/akkajvm"))
  .settings(akkaShared: _*)
  .settings(libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.3.6",
    "org.scalajs" %% "scalajs-pickling-play-json" % "0.4-SNAPSHOT"))


val rpcShared = transportShared ++ Seq(
  unmanagedSourceDirectories in Compile += baseDirectory.value / "../rpc",
  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "upickle" % "0.2.6-M3",
    "com.lihaoyi" %%% "autowire" % "0.2.4-M3"))

lazy val transportRpcJs = project.in(file("transport/rpcjs"))
  .settings(rpcShared: _*)
  .enablePlugins(ScalaJSPlugin)

lazy val transportRpcJvm = project.in(file("transport/rpcjvm"))
  .settings(rpcShared: _*)


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

val playWithScalaJs = Seq(
  unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared",
  unmanagedResourceDirectories in Compile += baseDirectory.value / "../js/src")

def scalaJsOfPlayProject(p: Project) = Seq(
  unmanagedSourceDirectories in Compile += (baseDirectory in p).value / "../shared",
  fastOptJS in Compile <<= (fastOptJS in Compile) triggeredBy (compile in (p, Compile)),
  crossTarget in (Compile, fastOptJS) := (baseDirectory in p).value / "public/javascripts",
  crossTarget in (Compile, fullOptJS) := (baseDirectory in p).value / "public/javascripts")


lazy val webRTCExample = project.in(file("examples/webrtc"))
  .settings(commonSettings: _*)
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(transportJavascript, transportAkkaJs)

lazy val reportListings = project.in(file("examples/report-listings"))
  .settings(commonSettings: _*)
  .dependsOn(transportRpcJvm, transportAkkaJvm, transportTyrus, transportNetty)


lazy val rpc = project.in(file("examples/rpc/jvm"))
  .enablePlugins(PlayScala)
  .settings((commonSettings ++ playWithScalaJs): _*)
  .dependsOn(transportPlay, transportRpcJvm, playTwoBrowsersTest % "test->test")
  .settings(libraryDependencies ++= Seq(
    "org.webjars" % "sockjs-client" % "0.3.4",
    "org.webjars" %% "webjars-play" % "2.3.0",
    "org.webjars" % "bootstrap" % "3.2.0"))

lazy val rpcJs = project.in(file("examples/rpc/js"))
  .settings((commonSettings ++ scalaJsOfPlayProject(rpc)): _*)
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(transportJavascript, transportRpcJs)
  .settings(libraryDependencies ++= Seq(
    "com.scalatags" %%% "scalatags" % "0.4.3-M3"))


lazy val chatWebSocket = project.in(file("examples/chat-websocket/jvm"))
  .enablePlugins(PlayScala)
  .settings((commonSettings ++ playWithScalaJs): _*)
  .dependsOn(transportPlay, transportAkkaJvm, playTwoBrowsersTest % "test->test")
  .settings(libraryDependencies ++= Seq(
    "org.webjars" %% "webjars-play" % "2.3.0",
    "org.webjars" % "sockjs-client" % "0.3.4",
    "org.webjars" % "jquery" % "2.1.1"))

lazy val chatWebSocketJs = project.in(file("examples/chat-websocket/js"))
  .settings((commonSettings ++ scalaJsOfPlayProject(chatWebSocket)): _*)
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(transportJavascript)
  .dependsOn(transportAkkaJs)
  .settings(libraryDependencies ++= Seq(
    "be.doeraene" %%% "scalajs-jquery" % "0.7.1-SNAPSHOT"))


lazy val chatWebRTC = project.in(file("examples/chat-webrtc/jvm"))
  .enablePlugins(PlayScala)
  .dependsOn(transportPlay, transportAkkaJvm, playTwoBrowsersTest % "test->test")
  .settings((commonSettings ++ playWithScalaJs): _*)
  .settings(libraryDependencies ++= Seq(
    "org.webjars" %% "webjars-play" % "2.3.0",
    "org.webjars" % "jquery" % "2.1.1"))

lazy val chatWebRTCJs = project.in(file("examples/chat-webrtc/js"))
  .settings((commonSettings ++ scalaJsOfPlayProject(chatWebRTC)): _*)
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(transportJavascript, transportAkkaJs)
  .settings(libraryDependencies ++= Seq(
    "be.doeraene" %%% "scalajs-jquery" % "0.7.1-SNAPSHOT"))


lazy val chatWebRTCFallback = project.in(file("examples/chat-webrtc-fallback/jvm"))
  .enablePlugins(PlayScala)
  .dependsOn(transportPlay, transportAkkaJvm, playTwoBrowsersTest % "test->test")
  .settings((commonSettings ++ playWithScalaJs): _*)
  .settings(libraryDependencies ++= Seq(
    "org.webjars" %% "webjars-play" % "2.3.0",
    "org.webjars" % "jquery" % "2.1.1"))

lazy val chatWebRTCFallbackJs = project.in(file("examples/chat-webrtc-fallback/js"))
  .settings((commonSettings ++ scalaJsOfPlayProject(chatWebRTCFallback)): _*)
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(transportJavascript, transportAkkaJs)
  .settings(libraryDependencies ++= Seq(
    "be.doeraene" %%% "scalajs-jquery" % "0.7.1-SNAPSHOT"))
