
import sbtrelease._
import ReleaseStateTransformations._
import com.typesafe.sbt.SbtStartScript
import gener8bundleBuild._

seq(SbtStartScript.startScriptForClassesSettings: _*)

seq(conscriptSettings :_*)

name := "gener8bundle"

organization := "ohnosequences"

version := "0.1.0"

scalaVersion := "2.10.0"

scalaBinaryVersion := "2.10.0"

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  if (v.trim.endsWith("SNAPSHOT"))
    Some(Resolver.file("local-snapshots", file("artifacts/snapshots.era7.com")))
  else
    Some(Resolver.file("local-releases", file("artifacts/releases.era7.com")))
}

resolvers ++= Seq (
                    "Typesafe Releases"   at "http://repo.typesafe.com/typesafe/releases",
                    "Sonatype Releases"   at "https://oss.sonatype.org/content/repositories/releases",
                    "Sonatype Snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots",
                    "Era7 Releases"       at "http://releases.era7.com.s3.amazonaws.com",
                    "Era7 Snapshots"      at "http://snapshots.era7.com.s3.amazonaws.com",
                    "Spray"               at "http://repo.spray.io"
                  )

libraryDependencies ++= Seq (
    "io.spray" %  "spray-json_2.10" % "1.2.3"
  )

scalacOptions ++= Seq(
                      "-feature",
                      "-language:higherKinds",
                      "-language:implicitConversions",
                      "-deprecation",
                      "-unchecked"
                    )
