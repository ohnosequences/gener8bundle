
import sbtrelease._
import ReleaseStateTransformations._
import com.typesafe.sbt.SbtStartScript
import gener8bundleBuild._

seq(SbtStartScript.startScriptForClassesSettings: _*)

seq(conscriptSettings :_*)

name := "gener8bundle"

organization := "ohnosequences"

version := "0.3.3"

scalaVersion := "2.9.1"

// scalaBinaryVersion := "2.10.0"

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
                    "Era7 Snapshots"      at "http://snapshots.era7.com.s3.amazonaws.com"
                  )

libraryDependencies ++= Seq (
                              "org.json4s" %% "json4s-native" % "3.1.0"
                            , "net.databinder.giter8" %% "giter8" % "0.5.3"
                            , "org.scala-sbt" % "launcher-interface" % "0.12.2"
                            )

scalacOptions ++= Seq(
                      "-deprecation",
                      "-unchecked"
                    )
