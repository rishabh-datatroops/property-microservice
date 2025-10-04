error id: file://<WORKSPACE>/graphql-service/build.sbt:`<error>`#`<error>`.
file://<WORKSPACE>/graphql-service/build.sbt
empty definition using pc, found symbol in pc: 
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -libraryDependencySchemes.
	 -libraryDependencySchemes#
	 -libraryDependencySchemes().
	 -scala/Predef.libraryDependencySchemes.
	 -scala/Predef.libraryDependencySchemes#
	 -scala/Predef.libraryDependencySchemes().
offset: 658
uri: file://<WORKSPACE>/graphql-service/build.sbt
text:
```scala
name := """graphql-service"""

version := "0.1"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)

scalaVersion := "2.13.16"

libraryDependencies ++= Seq(
  guice,
  "com.typesafe.play" %% "play-json" % "2.9.4",
  "com.typesafe.play" %% "play-ws" % "2.9.4",
  "com.typesafe.play" %% "play-ahc-ws" % "2.9.4",
  "com.typesafe.akka" %% "akka-stream" % "2.6.21",
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.21",
  "com.graphql-java" % "graphql-java" % "21.3",
  "com.graphql-java" % "graphql-java-extended-scalars" % "21.0",
  "org.slf4j" % "slf4j-log4j12" % "1.7.36"
)


// Resolve dependency conflicts
ThisBuild / libraryDependencySche@@mes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

```


#### Short summary: 

empty definition using pc, found symbol in pc: 