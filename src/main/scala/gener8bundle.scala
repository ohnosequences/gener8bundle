package ohnosequences.statica.gener8bundle

// This script parses given json bundle configuration,
// constructs parameters for giter8 and calls it

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
  val className = name.split("""([.-]|\s+)""").map(_.capitalize).mkString 
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

    (Seq(("name", name)) ++ 
     opt("bundle_version", bundle_version) ++
     opt("description", description) ++
     opt("org", org) ++
     opt("scala_version", scala_version) ++
     notEmpty("dependencies_sbt", dependencies_sbt(dependencies)) ++
     notEmpty("dependencies_class", dependencies_class(dependencies)) ++
     notEmpty("tool_version_sbt", ToolVersion(tool_version).forSbt) ++
     notEmpty("tool_version_class", ToolVersion(tool_version).forClass)
    ) map {case (k,v) => format(k,v)}
  }
}

/** The launched conscript entry point */
class App extends xsbti.AppMain {
  def run(config: xsbti.AppConfiguration) = {
    Exit(App.run(config.arguments))
  }
}

case class Exit(val code: Int) extends xsbti.Exit

object App {

  import scala.sys.process._
  import scala.io.Source
  import org.json4s._
  import org.json4s.native.JsonMethods._
  import giter8.Giter8

  /** Shared by the launched version and the runnable version,
   * returns the process status code */
  def run(args: Array[String]): Int = {
    if(args.length < 2) {
      println("Usage: gener8bundle <giter8 template address> <config_1.json> [... <config_n.json>]")
      return 1
    }
    else {
      val template = if (args(0) == "-") "ohnosequences/statica-bundle" else args(0)

      args.tail.foldLeft(0){ (result, file) =>
        // reading and parsing json
        val jsonConf = parse(Source.fromFile(file).mkString)
        // parsing it
        implicit val formats = DefaultFormats
        val conf = jsonConf.extract[BundleDescription]
        // constructing g8 command with arguments
        val g8cmd = template +: conf.toSeq
        println("g8 " + g8cmd.mkString(" "))
        // running it
        val r = Giter8.run(g8cmd.toArray)
        if (r == 0) result else r
      }
    }
  }

  /** Standard runnable class entrypoint */
  def main(args: Array[String]) {
    System.exit(run(args))
  }
}
