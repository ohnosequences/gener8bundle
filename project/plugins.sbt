resolvers += Resolver.url("Era7 ivy Releases", url("http://releases.era7.com.s3.amazonaws.com"))(Resolver.ivyStylePatterns)

addSbtPlugin("ohnosequences" % "sbt-s3-resolver" % "0.5.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.7")

addSbtPlugin("ohnosequences" % "sbt-buildinfo" % "0.3.2")

addSbtPlugin("net.databinder" % "conscript-plugin" % "0.3.5")
