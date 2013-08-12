resolvers ++= Seq (
  "Era7 Releases" at "http://releases.era7.com.s3.amazonaws.com"
)

addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.4.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.7")

addSbtPlugin("ohnosequences" % "sbt-buildinfo" % "0.3.2")

addSbtPlugin("net.databinder" % "conscript-plugin" % "0.3.5")
