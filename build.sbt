scalaVersion := "2.13.7"

name := "pioupiou"
organization := "com.github.mcsims"
version := "1.0"

val akkaVersion = "2.6.17"
val akkaHttpVersion = "10.1.11"

val scalaTestVersion = "3.2.10"

val circeVersion = "0.13.0"

libraryDependencies ++= Seq(
// Testing
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "org.scalatest" %% "scalatest-flatspec" % scalaTestVersion % "test",
// Akka (Actors system)
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
// Circe (JSON parsing)
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-optics" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
// slf4j
  "ch.qos.logback" % "logback-classic" % "1.2.7"
)
