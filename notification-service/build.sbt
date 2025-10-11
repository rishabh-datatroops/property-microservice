name := """notification-service"""

version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.16"

libraryDependencies ++= Seq(
  guice,
  "com.typesafe.play" %% "play-json" % "2.9.4",
  "com.typesafe.akka" %% "akka-stream-kafka" % "3.0.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test,
  "org.slf4j" % "slf4j-api" % "1.7.36"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
