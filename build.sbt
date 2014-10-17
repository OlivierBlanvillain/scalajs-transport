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
  .aggregate(actors)

lazy val actors = project.settings(commonSettings: _*)

lazy val transportSharedSettings = Seq(
  unmanagedSourceDirectories in Compile +=
    (baseDirectory in root).value / "transport/shared"
)

lazy val transportJvm = project.in(file("transport/play"))
  .settings(commonSettings: _*)
  .settings(transportSharedSettings: _*)

lazy val transportJs = project.in(file("transport/js"))
  .settings((commonSettings ++ scalaJSSettings): _*)
  .settings(transportSharedSettings: _*)

lazy val networkSharedSettings = Seq(
  unmanagedSourceDirectories in Compile +=
    (baseDirectory in root).value / "network/shared"
)

lazy val networkPlay = project.in(file("network/play"))
  .settings(commonSettings: _*)
  .settings(networkSharedSettings: _*)

lazy val networkJs = project.in(file("network/js"))
  .settings(commonSettings: _*)
  .settings(networkSharedSettings: _*)
  .dependsOn(actors)

lazy val examples = project.settings(commonSettings: _*)
  .aggregate(
    webRTCExample,
    chatWebSocket,
    chatWebRTC,
    autowire
  )

lazy val webRTCExample = project.in(file("examples/webrtc"))
  .settings(commonSettings: _*)
  .dependsOn(networkJs)
  .dependsOn(actors)

lazy val chatWebSocket = project.in(file("examples/chat-websocket"))
  .enablePlugins(PlayScala)
  .dependsOn(networkPlay)
  .settings(commonSettings: _*)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "cscommon")
  .settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "scalajs/src")

lazy val chatWebSocketScalaJS = project.in(file("examples/chat-websocket/scalajs"))
  .settings((commonSettings ++ scalaJSSettings): _*)
  .dependsOn(actors)
  .dependsOn(networkJs)
  .settings(
    unmanagedSourceDirectories in Compile +=
      (baseDirectory in chatWebSocket).value / "cscommon",
    fastOptJS in Compile <<= (fastOptJS in Compile) triggeredBy (compile in (chatWebSocket, Compile))
  )
  .settings(
    Seq(fastOptJS, fullOptJS) map {
      packageJSKey =>
        crossTarget in (Compile, packageJSKey) :=
          (baseDirectory in chatWebSocket).value / "public/javascripts"
    }: _*
  )

lazy val autowire = project.in(file("examples/autowire/jvm"))
  .enablePlugins(PlayScala)
  .dependsOn(transportJvm)
  .settings(commonSettings: _*)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "../shared")
  .settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "../js/src")

lazy val autowireScalaJS = project.in(file("examples/autowire/js"))
  .settings((commonSettings ++ scalaJSSettings): _*)
  .dependsOn(transportJs)
  .settings(
    unmanagedSourceDirectories in Compile +=
      (baseDirectory in autowire).value / "../shared",
    fastOptJS in Compile <<= (fastOptJS in Compile) triggeredBy (compile in (autowire, Compile))
  )
  .settings(
    Seq(fastOptJS, fullOptJS) map {
      packageJSKey =>
        crossTarget in (Compile, packageJSKey) :=
          (baseDirectory in autowire).value / "public/javascripts"
    }: _*
  )

lazy val chatWebRTC = project.in(file("examples/chat-webrtc"))
  .enablePlugins(PlayScala)
  .dependsOn(networkPlay)
  .settings(commonSettings: _*)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "cscommon")
  .settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "scalajs/src")

lazy val chatWebRTCScalaJS = project.in(file("examples/chat-webrtc/scalajs"))
  .settings((commonSettings ++ scalaJSSettings): _*)
  .dependsOn(actors)
  .dependsOn(networkJs)
  .settings(
    unmanagedSourceDirectories in Compile +=
      (baseDirectory in chatWebRTC).value / "cscommon",
    fastOptJS in Compile <<= (fastOptJS in Compile) triggeredBy (compile in (chatWebRTC, Compile))
  )
  .settings(
    Seq(fastOptJS, fullOptJS) map {
      packageJSKey =>
        crossTarget in (Compile, packageJSKey) :=
          (baseDirectory in chatWebRTC).value / "public/javascripts"
    }: _*
  )
