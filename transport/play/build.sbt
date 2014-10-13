resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe Maven Repository" at "http://repo.typesafe.com/typesafe/maven-releases/"
)

libraryDependencies ++= Seq(
  "org.webjars" % "sockjs-client" % "0.3.4",
  "com.github.fdimuccio" %% "play2-sockjs" % "0.3.0",
  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "com.typesafe.play" %% "play" % "2.3.5"
)
