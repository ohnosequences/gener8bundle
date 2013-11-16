Nice.scalaProject

name := "statika-cli"

description := "A command line tool for statika projects"

organization := "ohnosequences"

bucketSuffix := "era7.com"


libraryDependencies ++= Seq (
  "org.json4s" %% "json4s-native" % "3.2.5"
, "ohnosequences" %% "aws-scala-tools" % "0.2.3"
, "org.rogach" %% "scallop" % "0.9.4"
)


scalacOptions ++= Seq(
  "-language:reflectiveCalls"
, "-language:existentials"
)

// lint complains too much about scallop config stuff
scalacOptions ~= { opts => opts.filter(_ != "-Xlint") }


// sorry, no docs in code so far (see readme)
generateDocs := {}


buildInfoSettings

sourceGenerators in Compile <+= buildInfo

seq(conscriptSettings :_*)
