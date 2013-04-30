
import sbtrelease._
import ReleaseStateTransformations._
import gener8bundleBuild._

seq(conscriptSettings :_*)

name := "gener8bundle"

organization := "ohnosequences"

version := "0.7.0"

scalaVersion := "2.10.0"

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  if (v.trim.endsWith("SNAPSHOT"))
    Some(Resolver.file("local-snapshots", file("artifacts/snapshots.era7.com")))
  else
    Some(Resolver.file("local-releases", file("artifacts/releases.era7.com")))
}

resolvers ++= Seq (
                    "Typesafe Releases"   at "http://repo.typesafe.com/typesafe/releases"
                  , "Sonatype Releases"   at "https://oss.sonatype.org/content/repositories/releases"
                  , "Sonatype Snapshots"  at "https://oss.sonatype.org/content/repositories/snapshots"
                  , "Era7 Releases"       at "http://releases.era7.com.s3.amazonaws.com"
                  , "Era7 Snapshots"      at "http://snapshots.era7.com.s3.amazonaws.com"
                  , "spray repo"          at "http://repo.spray.io"
                  )

// resolvers += Resolver.file("local", file("/Users/laughedelic/.ivy2/local/"))

libraryDependencies ++= Seq (
                              "org.json4s" %% "json4s-native" % "3.1.0"
                            , "ohnosequences" %% "aws-scala-tools" % "0.2.0"
                            , "com.decodified" %% "scala-ssh" % "0.7.0"
                            , "com.github.scopt" %% "scopt" % "2.1.0"
                            )

//                              "org.json4s" % "json4s-native_2.10" % "3.1.0"
//                            //, "net.databinder.giter8" %% "giter8" % "0.5.3"
//                            //, "org.scala-sbt" % "launcher-interface" % "0.12.2"
//                            , "ohnosequences" % "aws-scala-tools_2.10" % "0.2.0"
//                            //, "com.decodified" % "scala-ssh_2.10" % "0.6.3"
//                            //, "fr.janalyse" % "janalyse-ssh_2.10" % "0.9.8" % "compile"
//                            , "com.github.scopt" % "scopt_2.10" % "2.1.0"

scalacOptions ++= Seq(
                      "-deprecation",
                      "-unchecked"
                    )
