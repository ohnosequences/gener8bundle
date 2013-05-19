package ohnosequences.statika.gener8bundle

object ConfigDescription {

  case class ToolVersion(ver: Option[String]) {
    val v = (ver map { x => if (x.isEmpty) None else Some(x) }).flatten
    def forSbt:   Option[String] = v.map("." + _)
    def forClass: Option[String] = v.map("_" + _.replaceAll("\\.", "_"))
  }

  def className(s: String) = s.split("""\W""").map(_.capitalize).mkString 

  case class BundleDependency(
      name: String
    , tool_version: Option[String]
    , bundle_version: Option[String]
    ) {
    val tv = ToolVersion(tool_version)
    val tvSbt = tv.forSbt.getOrElse("")
    val tvClass = tv.forClass.getOrElse("")
    val artifactName = name.toLowerCase.replaceAll("""\s+""", "-")

    val forSbt = "\"ohnosequences\" %% \"" + 
                    artifactName + tvSbt + "\" % \"" + 
                    bundle_version.getOrElse("0.1.0") + "\""
    val forClass = className(name) + tvClass
  }

  case class BundleDescription(
      name: String
    , publish_private: Boolean
    , description: Option[String]
    , org: Option[String]
    , bundle_version: Option[String]
    , tool_version: Option[String]
    , scala_version: Option[String]
    , statika_version: Option[String]
    , credentials: Option[String]
    , ami: BundleDependency
    , dependencies: List[BundleDependency]
    ) {

    def dependencies_sbt(l: List[BundleDependency]): Option[String] = 
      if (l.isEmpty) None
      else Some(l.map(_.forSbt).mkString("libraryDependencies ++= Seq(", ", ", ")"))

    def dependencies_class(l: List[BundleDependency]): Option[String] = 
      if (l.isEmpty) Some("HNil: HNil")
      else Some(l.map(_.forClass).mkString("", " :: ", " :: HNil"))

    def toSeq: Seq[String] = {
      def format(k: String, v: String) = "--" + k + "=" + v.toString.replaceAll(" ", "\\ ")
      def opt(k: String, v: Option[String]) = v.toList.map((k, _))

      (Seq( ("name", name)
          , ("private", publish_private.toString)
          , ("class_name", className(name))
          , ("ami", className(ami.forClass))
          ) ++
      (Seq( ("bundle_version", bundle_version)
          , ("description", description)
          , ("org", org)
          , ("scala_version", scala_version)
          , ("statika_version", statika_version)
          , ("credentials", credentials)
          , ("dependencies_sbt", dependencies_sbt(ami :: dependencies))
          , ("dependencies_class", dependencies_class(ami :: dependencies))
          , ("tool_version_sbt", ToolVersion(tool_version).forSbt)
          , ("tool_version_class", ToolVersion(tool_version).forClass)
          ) flatMap { case (k,v) => opt(k,v) })
      ) map { case (k,v) => format(k,v) }
    }
  }

}
