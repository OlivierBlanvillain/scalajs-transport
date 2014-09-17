import ScalaJSKeys._


val commonSettings = Seq(
  organization := "org.scalajs",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.11.2",
  normalizedName ~= { _.replace("scala-js", "scalajs") },
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature",
    "-encoding", "utf8"
  )
)

lazy val root = project.in(file(".")).settings(commonSettings: _*)
  .aggregate(actors)

lazy val actors = project.settings(commonSettings: _*)

lazy val networkSharedSettings = Seq(
  unmanagedSourceDirectories in Compile +=
    (baseDirectory in root).value / "network-shared" / "src" / "main" / "scala"
)

lazy val networkPlay = project.in(file("network-play"))
  .settings(commonSettings: _*)
  .settings(networkSharedSettings: _*)

lazy val networkJs = project.in(file("network-js"))
  .settings(commonSettings: _*)
  .settings(networkSharedSettings: _*)
  .dependsOn(actors)

lazy val examples = project.settings(commonSettings: _*)
  .aggregate(webworkersExample, faultToleranceExample,
      chatExample, chatExampleScalaJS,
      webRTCExample, anonymousChatWebSocket, anonymousChatWebSocketScalaJS, anonymousChatWebRTC, anonymousChatWebRTCScalaJS)

lazy val webworkersExample = project.in(file("examples/webworkers"))
  .settings(commonSettings: _*)
  .dependsOn(actors)

lazy val faultToleranceExample = project.in(file("examples/faulttolerance"))
  .settings(commonSettings: _*)
  .dependsOn(actors)

lazy val webRTCExample = project.in(file("examples/webrtc"))
  .settings(commonSettings: _*)
  .dependsOn(networkJs)
  .dependsOn(actors)

lazy val anonymousChatWebSocket = project.in(file("examples/anonymous-chat-websocket"))
  .enablePlugins(PlayScala)
  .dependsOn(networkPlay)
  .settings(commonSettings: _*)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "cscommon")
  .settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "scalajs/src")

lazy val anonymousChatWebSocketScalaJS = project.in(file("examples/anonymous-chat-websocket/scalajs"))
  .settings((commonSettings ++ scalaJSSettings): _*)
  .dependsOn(actors)
  .dependsOn(networkJs)
  .settings(
    unmanagedSourceDirectories in Compile +=
      (baseDirectory in anonymousChatWebSocket).value / "cscommon",
    fastOptJS in Compile <<= (fastOptJS in Compile) triggeredBy (compile in (anonymousChatWebSocket, Compile))
  )
  .settings(
    Seq(fastOptJS, fullOptJS) map {
      packageJSKey =>
        crossTarget in (Compile, packageJSKey) :=
          (baseDirectory in anonymousChatWebSocket).value / "public/javascripts"
    }: _*
  )

lazy val anonymousChatWebRTC = project.in(file("examples/anonymous-chat-webrtc"))
  .enablePlugins(PlayScala)
  .dependsOn(networkPlay)
  .settings(commonSettings: _*)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "cscommon")
  .settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "scalajs/src")
  .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)

lazy val anonymousChatWebRTCScalaJS = project.in(file("examples/anonymous-chat-webrtc/scalajs"))
  .settings((commonSettings ++ scalaJSSettings): _*)
  .dependsOn(actors)
  .dependsOn(networkJs)
  .settings(
    unmanagedSourceDirectories in Compile +=
      (baseDirectory in anonymousChatWebRTC).value / "cscommon",
    fastOptJS in Compile <<= (fastOptJS in Compile) triggeredBy (compile in (anonymousChatWebRTC, Compile))
  )
  .settings(
    Seq(fastOptJS, fullOptJS) map {
      packageJSKey =>
        crossTarget in (Compile, packageJSKey) :=
          (baseDirectory in anonymousChatWebRTC).value / "public/javascripts"
    }: _*
  )

lazy val chatExample = project.in(file("examples/chat-full-stack"))
  .enablePlugins(PlayScala)
  .dependsOn(networkPlay)
  .settings(commonSettings: _*)
  .settings(
    unmanagedSourceDirectories in Compile +=
      baseDirectory.value / "cscommon"
  )

lazy val chatExampleScalaJS = project.in(file("examples/chat-full-stack/scalajs"))
  .settings((commonSettings ++ scalaJSSettings): _*)
  .dependsOn(actors)
  .dependsOn(networkJs)
  .settings(
    unmanagedSourceDirectories in Compile +=
      (baseDirectory in chatExample).value / "cscommon",
    fastOptJS in Compile <<= (fastOptJS in Compile) triggeredBy (compile in (chatExample, Compile))
  )
  .settings(
    Seq(fastOptJS, fullOptJS) map {
      packageJSKey =>
        crossTarget in (Compile, packageJSKey) :=
          (baseDirectory in chatExample).value / "public/javascripts"
    }: _*
  )
