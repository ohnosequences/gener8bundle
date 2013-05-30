import sbtrelease._
import ReleaseStateTransformations._
import gener8bundleBuild._

seq(conscriptSettings :_*)

name := "gener8bundle"

organization := "ohnosequences"

scalaVersion := "2.10.0"

scalaBinaryVersion := "2.10.0"

publishMavenStyle := false

publishTo <<= (isSnapshot, s3resolver) { 
                (snapshot,   resolver) => 
  val prefix = if (snapshot) "snapshots" else "releases"
  resolver("Era7 "+prefix+" S3 bucket", "s3://"+prefix+".era7.com")
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
                            , "com.github.scopt" % "scopt_2.10" % "2.1.0"
                            )

scalacOptions ++= Seq(
                      "-deprecation",
                      "-unchecked"
                    )
