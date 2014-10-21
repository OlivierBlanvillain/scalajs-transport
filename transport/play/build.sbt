resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe Maven Repository" at "http://repo.typesafe.com/typesafe/maven-releases/",
  Resolver.url("scala-js-releases", url("http://dl.bintray.com/content/scala-js/scala-js-releases"))(Resolver.ivyStylePatterns)
)

libraryDependencies ++= Seq(
  "org.scalajs" %% "scalajs-pickling-play-json" % "0.3.1",
  "com.lihaoyi" %%% "upickle" % "0.2.2",
  "com.lihaoyi" %%% "autowire" % "0.2.1",
  "org.webjars" % "sockjs-client" % "0.3.4",
  "com.github.fdimuccio" %% "play2-sockjs" % "0.3.0",
  "com.typesafe.akka" %% "akka-actor" % "2.3.6",
  "com.typesafe.play" %% "play" % "2.3.5"
)
