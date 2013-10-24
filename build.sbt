import ohnosequences.sbt._

Era7.allSettings

name := "statika-cli"

organization := "ohnosequences"

scalaVersion := "2.10.3"

scalaBinaryVersion := "2.10.3"

bucketSuffix := "era7.com"


libraryDependencies ++= Seq (
  "org.json4s" % "json4s-native_2.10" % "3.1.0"
, "ohnosequences" % "aws-scala-tools_2.10" % "0.2.3"
, "org.rogach" % "scallop_2.10" % "0.9.2"
, "com.github.scala-incubator.io" % "scala-io-file_2.10" % "0.4.2"
)

scalacOptions ++= Seq(
  "-deprecation"
, "-unchecked"
, "-feature"
, "-language:reflectiveCalls"
, "-language:implicitConversions"
, "-language:existentials"
, "-language:postfixOps"
)

// sbt-buildinfo settings

buildInfoSettings

sourceGenerators in Compile <+= buildInfo


seq(conscriptSettings :_*)
