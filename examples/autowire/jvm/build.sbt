name := "autowire"

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-java" % "2.43.1",
  "com.github.detro.ghostdriver" % "phantomjsdriver" % "1.0.4" % "test",
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "bootstrap" % "3.2.0",
  "com.lihaoyi" %%% "upickle" % "0.2.2",
  "com.lihaoyi" %%% "autowire" % "0.2.1",
  "com.scalatags" %%% "scalatags" % "0.4.0"
)
