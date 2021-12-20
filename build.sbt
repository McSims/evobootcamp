scalaVersion := "2.13.6"

name := "hello-world"
organization := "ch.epfl.scala"
version := "1.0"

val http4sVersion = "0.21.22"

val catsVersion = "2.2.0"
val catsEffectVersion = "2.2.0"

val akkaVersion = "2.6.17"
val akkaHttpVersion = "10.1.11"
val akkaHttpCirceVersion = "1.31.0"

val scalaTestVersion = "3.2.10"

val circeVersion = "0.13.0"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "org.scalatest" %% "scalatest-flatspec" % scalaTestVersion % "test",
//
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
//
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-optics" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.7"
)
