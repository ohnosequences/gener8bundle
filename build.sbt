Nice.scalaProject

name := "statika-cli"
organization := "ohnosequences"
description := "Command line tools for Statika"

scalaVersion := "2.11.6"
//publishMavenStyle := true
bucketSuffix := "era7.com"

libraryDependencies ++= Seq (
  "ohnosequences" %% "aws-scala-tools" % "0.12.0",
  "org.rogach" %% "scallop" % "0.9.5"
)

enablePlugins(BuildInfoPlugin)
buildInfoKeys := Seq[BuildInfoKey](name, version)
