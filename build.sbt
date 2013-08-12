import sbtrelease._

seq(conscriptSettings :_*)

name := "gener8bundle"

organization := "ohnosequences"

scalaVersion := "2.10.2"

scalaBinaryVersion := "2.10.2"

publishTo <<= (isSnapshot, s3credentials) { 
                (snapshot,   credentials) => 
  val prefix = if (snapshot) "snapshots" else "releases"
  credentials map s3resolver("Era7 "+prefix+" S3 bucket", "s3://"+prefix+".era7.com")
}

resolvers ++= Seq (
  "Typesafe Releases"   at "http://repo.typesafe.com/typesafe/releases"
, "Sonatype Releases"   at "https://oss.sonatype.org/content/repositories/releases"
, "Sonatype Snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots"
, "Era7 Releases"       at "http://releases.era7.com.s3.amazonaws.com"
, "Era7 Snapshots"      at "http://snapshots.era7.com.s3.amazonaws.com"
)

libraryDependencies ++= Seq (
  "org.json4s" % "json4s-native_2.10" % "3.1.0"
, "ohnosequences" % "aws-scala-tools_2.10" % "0.2.3"
, "org.rogach" % "scallop_2.10" % "0.9.2"
// , "ohnosequences" % "giter8-lib_2.10" % "0.6.0"
, "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2"
)

scalacOptions ++= Seq(
  "-deprecation"
, "-unchecked"
, "-feature"
, "-language:reflectiveCalls"
, "-language:implicitConversions"
, "-language:existentials"
)

// sbt-release settings

releaseSettings

// sbt-buildinfo settings

buildInfoSettings

sourceGenerators in Compile <+= buildInfo
