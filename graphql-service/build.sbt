name := """graphql-service"""

version := "0.1"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalaVersion := "2.13.16"

libraryDependencies ++= Seq(
  guice,
  "com.typesafe.play" %% "play-json" % "2.9.4",
  "com.typesafe.akka" %% "akka-stream" % "2.6.21",
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.21",
  "com.graphql-java" % "graphql-java" % "21.3",
  "com.graphql-java" % "graphql-java-extended-scalars" % "21.0",
  "org.reactivestreams" % "reactive-streams" % "1.0.4",
  "org.slf4j" % "slf4j-api" % "1.7.36"
)

// Add resources directory to classpath
Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"

// Resolve dependency conflicts
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always