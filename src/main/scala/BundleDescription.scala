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

  def startCase(s: String) = s.toLowerCase.split(" ").map(_.capitalize).mkString(" ")
  def wordOnly(s: String) = s.replaceAll("""\W""", "")
  def upperCamel(s: String) = wordOnly(startCase(s))
  def normalize(s: String) = hyphenate(s.toLowerCase)
  def hyphenate(s: String) = s.replaceAll("""\s+""", "-")

  val forSbt = "\"ohnosequences\" %% \"" + 
                  normalize(name) + ToolVersion(tool_version).forSbt + "\" % \"" + 
                  bundle_version.getOrElse("0.1.0-SNAPSHOT") + "\""
  val forClass = upperCamel(name) + ToolVersion(tool_version).forClass
}

case class BundleDescription(
    name: String
  , bundle_version: Option[String]
  , tool_version: Option[String]
  , description: Option[String]
  , org: Option[String]
  , scala_version: Option[String]
  , dependencies: List[BundleDependency]
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

    (Seq(("name", name))
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
