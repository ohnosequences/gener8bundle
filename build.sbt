
import sbtrelease._
import ReleaseStateTransformations._

import gener8bundleBuild._

name := "gener8bundle"

organization := "ohnosequences"

version := "0.1.0"

scalaVersion := "2.10.0"

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
                              "com.chuusai" %% "shapeless" % "1.2.3"
                            , "net.databinder.giter8" % "giter8_2.9.1" % "0.5.3"
                            , "org.scala-sbt" % "launcher-interface" % "0.12.1"
                            // , "org.eclipse.jgit" % "org.eclipse.jgit" % "1.3.0.201202151440-r"
                            )

scalacOptions ++= Seq(
                      "-feature",
                      "-language:higherKinds",
                      "-language:implicitConversions",
                      "-deprecation",
                      "-unchecked"
                    )
