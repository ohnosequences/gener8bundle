package ohnosequences.statika.cli

object BundleDescription {

  def nonEmpty(s: Option[String]) = if (s == Some("")) None else s

  def artifactName(s: String) = s.toLowerCase.replaceAll("""\s+""", "-")
  def objectName(s: String) = s.split("""\W""").map(_.capitalize).mkString

  case class BundleEntity(org: String, name: String, version: String)

  case class DescriptionFormat(
      bundle: BundleEntity
    , sbtStatikaPlugin: BundleEntity
    , dependencies: List[BundleEntity] = List()
    ) {

    def dependencies_sbt(l: List[BundleEntity]): Option[String] = 
      if (l.isEmpty) None
      else Some(l.map{b => 
        s""" "${b.org}" %% "${artifactName(b.name)}" % "${b.version}" """
        }.mkString("libraryDependencies ++= Seq(", ", ", ")"))

    def dependencies_class(l: List[BundleEntity]): Option[String] = 
      if (l.isEmpty) None
      else Some(l.map{b => objectName(b.name)}.mkString("", " :: ", " :: HNil"))

    def toSeq: Seq[String] = {
      def format(k: String, v: String) = "--" + k + "=" + v.toString.replaceAll(" ", "\\ ")
      def opt(k: String, v: Option[String]) = v.toList.map((k, _))

      val sp = sbtStatikaPlugin

      (Seq( ("name", artifactName(bundle.name))
          , ("object_name", objectName(bundle.name))
          , ("version", bundle.version)
          , ("org", bundle.org)
          , ("sbt_statika_plugin", s""" "${sp.org}" % "${artifactName(sp.name)}" % "${sp.version}" """)
          ) ++
      (Seq( ("dependencies_sbt", dependencies_sbt(dependencies))
          , ("dependencies_class", dependencies_class(dependencies))
          ) flatMap { case (k,v) => opt(k,v) })
      ) map { case (k,v) => format(k,v) }
    }
  }

}
