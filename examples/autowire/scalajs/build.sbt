name := "Scala.js actors examples - autowire"

normalizedName := "autowire"

libraryDependencies ++= Seq(
  "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6",
  "com.lihaoyi" %%% "upickle" % "0.2.2",
  "com.lihaoyi" %%% "autowire" % "0.2.1",
  "com.scalatags" %%% "scalatags" % "0.4.0"
)
