addSbtPlugin("org.scala-lang.modules.scalajs" % "scalajs-sbt-plugin" % "0.5.4")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.4")

//to generate eclipse project
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")

//to generate idea project
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")
