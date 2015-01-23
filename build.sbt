lazy val commonSettings = Seq(
  organization := "org.scalajs",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.11.4",
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

lazy val buildSettings = Defaults.defaultSettings ++ Seq(javaOptions += "-Xmx1G")

parallelExecution in Global := false

lazy val root = project
  .in(file("."))
  .settings(commonSettings: _*)
  .aggregate(
    transportCoreJS,
    transportCoreJVM,
    transportJavaScriptJS,
    transportJavaScriptJVM,
    transportWebRTCJS,
    transportWebRTCJVM,
    transportNettyJS,
    transportNettyJVM,
    transportPlayJS,
    transportPlayJVM,
    transportTyrusJS,
    transportTyrusJVM,
    transportAkkaJS,
    transportAkkaJVM,
    transportRPCJS,
    transportRPCJVM)

lazy val examples = project
  .in(file("examples"))
  .settings(commonSettings: _*)
  .aggregate(
    transportTestJVM,
    transportTestPlayJVM,
    reportListingsJVM,
    exampleRPCJVM,
    chatWebSocketJVM,
    chatWebRTCJVM,
    chatWebRTCClientFallbackJVM)

// Transport

lazy val transportCore = crossProject
  .crossType(CrossType.Full)
  .in(file("transport/core"))
  .settings(commonSettings: _*)
  .settings(name := "transport-core")
lazy val transportCoreJVM = transportCore.jvm
lazy val transportCoreJS = transportCore.js

lazy val transportJavaScript = crossProject
  .crossType(CrossType.Dummy)
  .in(file("transport/javascript"))
  .settings(commonSettings: _*)
  .settings(name := "transport-javascript")
  .dependsOn(transportCore)
  .jsSettings(libraryDependencies +=
    "org.scala-js" %%% "scalajs-dom" % "0.7.0")
lazy val transportJavaScriptJVM = transportJavaScript.jvm
lazy val transportJavaScriptJS = transportJavaScript.js

lazy val transportWebRTC = crossProject
  .crossType(CrossType.Full)
  .in(file("transport/webrtc"))
  .settings(commonSettings: _*)
  .settings(name := "transport-webrtc")
  .dependsOn(transportCore)
  .jsSettings(libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.7.0",
    // TODO: These two should go away at some point.
    "org.scalajs" %%% "scalajs-pickling" % "0.4-SNAPSHOT",
    "org.scalajs" %%% "scalajs-actors" % "0.1-SNAPSHOT"))
lazy val transportWebRTCJVM = transportWebRTC.jvm
lazy val transportWebRTCJS = transportWebRTC.js

lazy val transportNetty = crossProject
  .crossType(CrossType.Dummy)
  .in(file("transport/netty"))
  .settings(commonSettings: _*)
  .settings(name := "transport-netty")
  .dependsOn(transportCore)
  .jvmSettings(libraryDependencies +=
    "io.netty" % "netty-all" % "4.0.24.Final")
lazy val transportNettyJVM = transportNetty.jvm
lazy val transportNettyJS = transportNetty.js

lazy val transportPlay = crossProject
  .crossType(CrossType.Dummy)
  .in(file("transport/play"))
  .settings(commonSettings: _*)
  .settings(name := "transport-play")
  .dependsOn(transportCore)
  .jvmSettings(libraryDependencies ++= Seq(
    "com.github.fdimuccio" %% "play2-sockjs" % "0.3.0",
    "com.typesafe.akka" %% "akka-actor" % "2.3.6",
    "com.typesafe.play" %% "play" % "2.3.5"))
lazy val transportPlayJVM = transportPlay.jvm
lazy val transportPlayJS = transportPlay.js

lazy val transportTyrus = crossProject
  .crossType(CrossType.Dummy)
  .in(file("transport/tyrus"))
  .settings(commonSettings: _*)
  .settings(name := "transport-tyrus")
  .dependsOn(transportCore)
  .jvmSettings(libraryDependencies +=
    "org.glassfish.tyrus.bundles" % "tyrus-standalone-client" % "1.8.3")
lazy val transportTyrusJVM = transportTyrus.jvm
lazy val transportTyrusJS = transportTyrus.js

lazy val transportAkka = crossProject
  .crossType(CrossType.Full)
  .in(file("transport/akka"))
  .settings(commonSettings: _*)
  .settings(name := "transport-akka")
  .dependsOn(transportCore)
  .jvmSettings(libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.3.6",
    "org.scalajs" %% "scalajs-pickling-play-json" % "0.4-SNAPSHOT"))
  .jsSettings(libraryDependencies ++= Seq(
    "org.scalajs" %%% "scalajs-pickling" % "0.4-SNAPSHOT",
    "org.scalajs" %%% "scalajs-actors" % "0.1-SNAPSHOT"))
lazy val transportAkkaJVM = transportAkka.jvm
lazy val transportAkkaJS = transportAkka.js

lazy val transportRPC = crossProject
  .crossType(CrossType.Pure)
  .in(file("transport/rpc"))  
  .settings(commonSettings: _*)
  .settings(name := "transport-rpc")
  .dependsOn(transportCore)
  .settings(libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "upickle" % "0.2.6-M3",
    "com.lihaoyi" %%% "autowire" % "0.2.4-M3"))
lazy val transportRPCJVM = transportRPC.jvm
lazy val transportRPCJS = transportRPC.js

lazy val transportTestPlayJVM = project.in(file("transport/test-play"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayScala)
  .settings(libraryDependencies ++= Seq(
    "org.seleniumhq.selenium" % "selenium-java" % "2.43.1",
    "com.github.detro.ghostdriver" % "phantomjsdriver" % "1.0.4" % "test"))
  
lazy val transportTestJVM = project.in(file("transport/test"))
  .settings(commonSettings: _*)
  .dependsOn(transportNettyJVM)
  .dependsOn(transportTyrusJVM)
  .settings(libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"))
  

// Cross Play/Scala.JS settings

lazy val sharedPlayScalaJS = Seq(
  unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared/src/main/scala",
  unmanagedSourceDirectories in Test += baseDirectory.value / "../shared/src/test/scala",
  unmanagedResourceDirectories in Compile += baseDirectory.value / "../shared/src/main/resources",
  unmanagedResourceDirectories in Test += baseDirectory.value / "../shared/src/test/resources")

lazy val UK = new com.typesafe.sbt.packager.universal.UniversalKeys{}

def playWithScalaJS(scalaJSProject: Project) = Seq(
  UK.dist <<= UK.dist dependsOn (fullOptJS in (scalaJSProject, Compile)),
  UK.stage <<= UK.stage dependsOn (fullOptJS in (scalaJSProject, Compile)),
  compile in Compile <<= (compile in Compile) dependsOn (fastOptJS in (scalaJSProject, Compile))
) ++ sharedPlayScalaJS

lazy val scalaJSWithPlay = Seq(
  crossTarget in (Compile, packageScalaJSLauncher) := baseDirectory.value / "../jvm/public/javascripts",
  crossTarget in (Compile, fastOptJS) := baseDirectory.value / "../jvm/public/javascripts",
  crossTarget in (Compile, fullOptJS) := baseDirectory.value / "../jvm/public/javascripts"
) ++ sharedPlayScalaJS


// Examples

lazy val reportListings = crossProject
  .crossType(CrossType.Dummy)
  .in(file("examples/report-listings"))
  .settings(commonSettings: _*)
  .dependsOn(transportRPC, transportAkka, transportTyrus, transportNetty, transportWebRTC)
lazy val reportListingsJVM = reportListings.jvm
lazy val reportListingsJS = reportListings.js

lazy val rawWebRTC = crossProject
  .crossType(CrossType.Dummy)
  .in(file("examples/raw-webrtc"))
  .settings(commonSettings: _*)
  .dependsOn(transportJavaScript, transportAkka, transportWebRTC)
lazy val rawWebRTCJVM = rawWebRTC.jvm
lazy val rawWebRTCJS = rawWebRTC.js

lazy val exampleRPCJS = project
  .in(file("examples/rpc/js"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(scalaJSWithPlay: _*)
  .dependsOn(transportJavaScriptJS, transportPlayJS, transportRPCJS)
  .settings(libraryDependencies ++= Seq(
    "com.scalatags" %%% "scalatags" % "0.4.3-M3"))
lazy val exampleRPCJVM = project
  .in(file("examples/rpc/jvm"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(playWithScalaJS(exampleRPCJS): _*)
  .dependsOn(transportPlayJVM, transportRPCJVM, transportTestPlayJVM % "test->test")
  .settings(libraryDependencies ++= Seq(
    "org.webjars" % "sockjs-client" % "0.3.4",
    "org.webjars" %% "webjars-play" % "2.3.0",
    "org.webjars" % "bootstrap" % "3.2.0"))

lazy val chatWebSocketJS = project
  .in(file("examples/chat-websocket/js"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(scalaJSWithPlay: _*)
  .dependsOn(transportJavaScriptJS, transportPlayJS, transportAkkaJS)
  .settings(libraryDependencies ++= Seq(
    "be.doeraene" %%% "scalajs-jquery" % "0.7.1-SNAPSHOT"))
lazy val chatWebSocketJVM = project
  .in(file("examples/chat-websocket/jvm"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(playWithScalaJS(chatWebSocketJS): _*)
  .dependsOn(transportPlayJVM, transportAkkaJVM, transportTestPlayJVM % "test->test")
  .settings(libraryDependencies ++= Seq(
    "org.webjars" %% "webjars-play" % "2.3.0",
    "org.webjars" % "sockjs-client" % "0.3.4",
    "org.webjars" % "jquery" % "2.1.1"))

lazy val chatWebRTCJS = project
  .in(file("examples/chat-webrtc/js"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(scalaJSWithPlay: _*)
  .dependsOn(transportJavaScriptJS, transportPlayJS, transportAkkaJS, transportWebRTCJS)
  .settings(libraryDependencies ++= Seq(
    "be.doeraene" %%% "scalajs-jquery" % "0.7.1-SNAPSHOT"))
lazy val chatWebRTCJVM = project
  .in(file("examples/chat-webrtc/jvm"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(playWithScalaJS(chatWebRTCJS): _*)
  .dependsOn(transportPlayJVM, transportAkkaJVM, transportTestPlayJVM % "test->test")
  .settings(libraryDependencies ++= Seq(
    "org.webjars" %% "webjars-play" % "2.3.0",
    "org.webjars" % "jquery" % "2.1.1"))

lazy val chatWebRTCClientFallbackJS = project
  .in(file("examples/chat-webrtc-fallback/js"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(scalaJSWithPlay: _*)
  .dependsOn(transportJavaScriptJS, transportPlayJS, transportAkkaJS, transportWebRTCJS)
  .settings(libraryDependencies ++= Seq(
    "be.doeraene" %%% "scalajs-jquery" % "0.7.1-SNAPSHOT"))
lazy val chatWebRTCClientFallbackJVM = project
  .in(file("examples/chat-webrtc-fallback/jvm"))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(playWithScalaJS(chatWebRTCClientFallbackJS): _*)
  .dependsOn(transportPlayJVM, transportAkkaJVM, transportTestPlayJVM % "test->test")
  .settings(libraryDependencies ++= Seq(
    "org.webjars" %% "webjars-play" % "2.3.0",
    "org.webjars" % "jquery" % "2.1.1"))
