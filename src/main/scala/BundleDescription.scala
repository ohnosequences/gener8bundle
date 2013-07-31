package ohnosequences.statika.gener8bundle

object ConfigDescription {

  case class ToolVersion(v: Option[String]) {
    def forSbt:   Option[String] = nonEmpty(v).map("." + _)
    def forClass: Option[String] = nonEmpty(v).map("_" + _.replaceAll("\\.", "_"))
  }

  def nonEmpty(s: Option[String]) = if (s == Some("")) None else s

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
    , is_private: Boolean
    , description: Option[String]
    , org: Option[String]
    , tool_version: Option[String]
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
      val tv = ToolVersion(tool_version)
      val tvSbt = tv.forSbt.getOrElse("")
      val tvClass = tv.forClass.getOrElse("")

      (Seq( ("name", name + tvSbt)
          , ("class_name", className(name) + tvClass)
          , ("is_private", is_private.toString)
          ) ++
      (Seq( ("description", nonEmpty(description))
          , ("org", nonEmpty(org))
          , ("dependencies_sbt", dependencies_sbt(dependencies))
          , ("dependencies_class", dependencies_class(dependencies))
          ) flatMap { case (k,v) => opt(k,v) })
      ) map { case (k,v) => format(k,v) }
    }
  }

}
