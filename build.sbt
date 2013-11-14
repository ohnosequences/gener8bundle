Nice.scalaProject

name := "statika-cli"

organization := "ohnosequences"

scalaBinaryVersion := scalaVersion.value

bucketSuffix := "era7.com"


libraryDependencies ++= Seq (
  "org.json4s" % "json4s-native_2.10" % "3.1.0"
, "ohnosequences" % "aws-scala-tools_2.10" % "0.2.3"
, "org.rogach" % "scallop_2.10" % "0.9.2"
)

scalacOptions ++= Seq(
  "-language:reflectiveCalls"
, "-language:existentials"
)

// sbt-buildinfo settings

buildInfoSettings

sourceGenerators in Compile <+= buildInfo


seq(conscriptSettings :_*)
