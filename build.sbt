Nice.scalaProject

name := "statika-cli"

description := "A command line tool for statika projects"

organization := "ohnosequences"

bucketSuffix := "era7.com"

libraryDependencies ++= Seq (
  "org.json4s" %% "json4s-native" % "3.1.0"
, "ohnosequences" %% "aws-scala-tools" % "0.2.3"
, "org.rogach" %% "scallop" % "0.9.2"
)

scalacOptions ++= Seq(
  "-language:reflectiveCalls"
, "-language:existentials"
)

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

seq(conscriptSettings :_*)
