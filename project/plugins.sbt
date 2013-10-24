resolvers += "Era7 maven releases"  at "http://releases.era7.com.s3.amazonaws.com"

addSbtPlugin("ohnosequences" % "era7-sbt-release" % "0.1.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.2.5")

addSbtPlugin("net.databinder" % "conscript-plugin" % "0.3.5")
