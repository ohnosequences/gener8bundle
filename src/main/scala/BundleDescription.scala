package ohnosequences.statika.gener8bundle

case class ToolVersion(v: Option[String]) {
  def forSbt:   String = v.map("." + _).getOrElse("")
  def forClass: String = v.map("_" + _.replaceAll("\\.", "_")).getOrElse("")
}

case class BundleDependency(
    name: String
  , tool_version: Option[String]
  , bundle_version: Option[String]
  ) {
  val tv = ToolVersion(tool_version)
  val className = name.split("""\W""").map(_.capitalize).mkString 
  val artifactName = name.toLowerCase.replaceAll("""\s+""", "-")

  val forSbt = "\"ohnosequences\" %% \"" + 
                  artifactName + tv.forSbt + "\" % \"" + 
                  bundle_version.getOrElse("0.1.0") + "\""
  val forClass = className + tv.forClass
}

case class BundleDescription(
    name: String
  , bundle_version: Option[String]
  , tool_version: Option[String]
  , description: Option[String]
  , org: Option[String]
  , scala_version: Option[String]
  , dependencies: List[BundleDependency]
  , publish_private: Boolean
  ) {

  def dependencies_sbt(l: List[BundleDependency]): String = 
    if (l.isEmpty) ""
    else l.map(_.forSbt).mkString("libraryDependencies ++= Seq(", ", ", ")")

  def dependencies_class(l: List[BundleDependency]): String = 
    if (l.isEmpty) "HNil: HNil"
    else l.map(_.forClass).mkString("", " :: ", " :: HNil")

  def toSeq: Seq[String] = {
    def format(k: String, v: String) = "--" + k + "=" + v.toString.replaceAll(" ", "\\ ")
    def opt[A](k: String, v: Option[A]) = v.toList.map((k, _))
    def notEmpty(k: String, v: String) = if (v.isEmpty) Seq() else Seq((k, v))

    (Seq( ("name", name)
        , ("private", publish_private.toString)
        , ("className", name.split("""\W""").map(_.capitalize).mkString)
        )
    ++ opt("bundle_version", bundle_version)
    ++ opt("description", description)
    ++ opt("org", org)
    ++ opt("scala_version", scala_version)
    ++ notEmpty("dependencies_sbt", dependencies_sbt(dependencies))
    ++ notEmpty("dependencies_class", dependencies_class(dependencies))
    ++ notEmpty("tool_version_sbt", ToolVersion(tool_version).forSbt)
    ++ notEmpty("tool_version_class", ToolVersion(tool_version).forClass)
    ) map {case (k,v) => format(k,v)}
  }
}
