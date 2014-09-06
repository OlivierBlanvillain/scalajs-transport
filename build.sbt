import ScalaJSKeys._

val commonSettings = Seq(
    organization := "org.scalajs",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.10.3",
    normalizedName ~= { _.replace("scala-js", "scalajs") },
    scalacOptions ++= Seq(
        "-deprecation",
        "-unchecked",
        "-feature",
        "-encoding", "utf8"
    )
)

lazy val root = project.in(file(".")).settings(commonSettings: _*)
  .aggregate(actors, jsNetwork, playNetwork)

lazy val actors = project.settings(commonSettings: _*)
  .settings(
      unmanagedSourceDirectories in Compile +=
        (sourceDirectory in Compile).value / "wscommon"
  )

lazy val playNetwork = project.in(file("play-network"))
  .settings(commonSettings: _*)
  .settings(
      unmanagedSourceDirectories in Compile +=
        (sourceDirectory in (actors, Compile)).value / "wscommon"
  )

lazy val jsNetwork = project.in(file("js-network"))
  .settings(commonSettings: _*)
  .dependsOn(actors)

lazy val examples = project.settings(commonSettings: _*)
  .aggregate(webworkersExample, faultToleranceExample,
      chatExample, chatExampleScalaJS,
      webrtcExample, anonymousChatWebsocket, anonymousChatWebsocketScalaJS, anonymousChatWebrtc, anonymousChatWebrtcScalaJS)

lazy val webworkersExample = project.in(file("examples/webworkers"))
  .settings(commonSettings: _*)
  .dependsOn(actors)

lazy val faultToleranceExample = project.in(file("examples/faulttolerance"))
  .settings(commonSettings: _*)
  .dependsOn(actors)

lazy val webrtcExample = project.in(file("examples/webrtc"))
  .settings(commonSettings: _*)
  .dependsOn(jsNetwork)
  .dependsOn(actors)

lazy val anonymousChatWebsocket = project.in(file("examples/anonymous-chat-websocket"))
  .enablePlugins(PlayScala)
  .dependsOn(playNetwork)
  .settings(commonSettings: _*)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "cscommon")
  .settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "scalajs/src")

lazy val anonymousChatWebsocketScalaJS = project.in(file("examples/anonymous-chat-websocket/scalajs"))
  .settings((commonSettings ++ scalaJSSettings): _*)
  .dependsOn(actors)
  .dependsOn(jsNetwork)
  .settings(
      unmanagedSourceDirectories in Compile +=
        (baseDirectory in anonymousChatWebsocket).value / "cscommon",
      fastOptJS in Compile <<= (fastOptJS in Compile) triggeredBy (compile in (anonymousChatWebsocket, Compile))
  )
  .settings(
      Seq(fastOptJS, fullOptJS) map {
        packageJSKey =>
          crossTarget in (Compile, packageJSKey) :=
            (baseDirectory in anonymousChatWebsocket).value / "public/javascripts"
      }: _*
  )

lazy val anonymousChatWebrtc = project.in(file("examples/anonymous-chat-webrtc"))
  .enablePlugins(PlayScala)
  .dependsOn(playNetwork)
  .settings(commonSettings: _*)
  .settings(unmanagedSourceDirectories in Compile += baseDirectory.value / "cscommon")
  .settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "scalajs/src")

lazy val anonymousChatWebrtcScalaJS = project.in(file("examples/anonymous-chat-webrtc/scalajs"))
  .settings((commonSettings ++ scalaJSSettings): _*)
  .dependsOn(actors)
  .dependsOn(jsNetwork)
  .settings(
      unmanagedSourceDirectories in Compile +=
        (baseDirectory in anonymousChatWebrtc).value / "cscommon",
      fastOptJS in Compile <<= (fastOptJS in Compile) triggeredBy (compile in (anonymousChatWebrtc, Compile))
  )
  .settings(
      Seq(fastOptJS, fullOptJS) map {
        packageJSKey =>
          crossTarget in (Compile, packageJSKey) :=
            (baseDirectory in anonymousChatWebrtc).value / "public/javascripts"
      }: _*
  )

lazy val chatExample = project.in(file("examples/chat-full-stack"))
  .enablePlugins(PlayScala)
  .dependsOn(playNetwork)
  .settings(commonSettings: _*)
  .settings(
      unmanagedSourceDirectories in Compile +=
        baseDirectory.value / "cscommon"
  )

lazy val chatExampleScalaJS = project.in(file("examples/chat-full-stack/scalajs"))
  .settings((commonSettings ++ scalaJSSettings): _*)
  .dependsOn(actors)
  .dependsOn(jsNetwork)
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
