name := "listing-service"

version := "0.1"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalaVersion := "2.13.16"

libraryDependencies ++= Seq(
  guice,
  "com.typesafe.play" %% "play-slick" % "5.2.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.2.0",
  "org.postgresql" % "postgresql" % "42.6.0",
  "com.typesafe.play" %% "play-json" % "2.9.4",
  // Use traditional Akka Kafka which is more stable
  "com.typesafe.akka" %% "akka-stream-kafka" % "2.1.1",
  "org.apache.kafka" % "kafka-clients" % "2.8.2",
  "org.slf4j" % "slf4j-api" % "1.7.36"
)
