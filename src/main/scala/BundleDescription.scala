package ohnosequences.statika.cli

object BundleDescription {

  def nonEmpty(s: Option[String]) = if (s == Some("")) None else s

  def asArtifact(s: String) = s.toLowerCase.replaceAll("""\s+""", "-")
  def asObject(s: String) = s.split("""\W""").map(_.capitalize).mkString

  case class BundleEntity(org: String, name: String, version: String, objectName: Option[String] = None) {
    val artifact = asArtifact(name)
    val obj = objectName.getOrElse(asObject(name))
  }

  case class DescriptionFormat(
      bundle: BundleEntity
    , sbtStatikaPlugin: BundleEntity
    , dependencies: List[BundleEntity] = List()
    ) {

    def dependencies_sbt(l: List[BundleEntity]): Option[String] = 
      if (l.isEmpty) None
      else Some(l.map{b => 
          s""" "${b.org}" %% "${b.artifact}" % "${b.version}" """
        }.mkString("libraryDependencies ++= Seq(", ", ", ")"))

    def dependencies_class(l: List[BundleEntity]): Option[String] = 
      if (l.isEmpty) None
      else Some(l.map{ b => b.obj }.mkString("", " :: ", " :: HNil"))

    def toSeq: Seq[String] = {
      def format(k: String, v: String) = "--" + k + "=" + v.toString.replaceAll(" ", "\\ ")
      def opt(k: String, v: Option[String]) = v.toList.map((k, _))

      val sp = sbtStatikaPlugin

      (Seq( ("name", bundle.artifact)
          , ("object_name", bundle.obj)
          , ("version", bundle.version)
          , ("org", bundle.org)
          , ("sbt_statika_plugin", s""" "${sp.org}" % "${sp.artifact}" % "${sp.version}" """)
          ) ++
      (Seq( ("dependencies_sbt", dependencies_sbt(dependencies))
          , ("dependencies_class", dependencies_class(dependencies))
          ) flatMap { case (k,v) => opt(k,v) })
      ) map { case (k,v) => format(k,v) }
    }
  }

}
