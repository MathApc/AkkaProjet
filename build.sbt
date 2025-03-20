
import Dependencies._

ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

//enablePlugins(JavaAppPackaging)

val akkaVersion = "2.6.20"
val akkaHttpVersion = "10.2.10"

scalaVersion := "2.13.12"

libraryDependencies += "ch.megard" %% "akka-http-cors" % "1.1.3"



libraryDependencies ++= Seq(
  // Akka Actor et Streams
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,

  "com.github.jwt-scala" %% "jwt-core" % "9.2.0",

  // Akka HTTP
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

  // Spray JSON
  "io.spray" %% "spray-json" % "1.3.6",

  

  // JWT pour l'authentification (CORRECTION DE L'ERREUR)
  "com.github.jwt-scala" %% "jwt-core" % "9.2.0",

  // JDBC pour MySQL
  "mysql" % "mysql-connector-java" % "8.0.33",

  // Akka HTTP Test (optionnel)
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,

  "org.scalatest" %% "scalatest" % "3.2.15" % Test,

  

  // Logging
  "ch.qos.logback" % "logback-classic" % "1.4.5"
)


// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
