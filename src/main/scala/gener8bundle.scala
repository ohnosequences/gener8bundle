package ohnosequences.statica.gener8bundle

// This script parses given json bundle configuration,
// constructs parameters for giter8 and calls it

case class BundleDescription(
    name: String
  , version: Option[String]
  , description: Option[String]
  , org: Option[String]
  , scala_version: Option[String]
  , dependencies: List[String]
  ) {

  import giter8.G8._

  def depsImport(l: List[String]): String = 
  if (l.isEmpty) " "
  else l.map(upperCamel).mkString(
    "import ohnosequences.statica.bundles.{", ", ", "}")

  def depsHList(l: List[String]): String = 
  if (l.isEmpty) "HNil: HNil"
  else l.map(upperCamel).map(_ + ".Bundle").mkString("flatten(", " :: ", " :: HNil)")

  def depsSbt(l: List[String]): String = 
  if (l.isEmpty) " "
  else l.map(normalize).map(
      "\"ohnosequences\" %% \"" + _ + "\" % \"0.1.0-SNAPSHOT\"" // TODO: use actual version
      ).mkString("libraryDependencies ++= Seq(", ", ", ")")

  def toSeq: Seq[String] = {
    def format(k: String, v: String) = "--" + k + "=" + v.toString.replaceAll(" ", "\\ ")
    def opt[A](k: String, v: Option[A]) = v.toList.map((k, _))

    (Seq(("name", name)) ++ 
     opt("version", version) ++
     opt("description", description) ++
     opt("org", org) ++
     opt("scala_version", scala_version) ++
      ( if (dependencies.isEmpty) Seq()
        else Seq(("depsSbt", depsSbt(dependencies)),
                 ("depsImport", depsImport(dependencies)),
                 ("depsHList", depsHList(dependencies)))
      )
    ) map {case (k,v) => format(k,v)}
  }
}

/** The launched conscript entry point */
class App extends xsbti.AppMain {
  def run(config: xsbti.AppConfiguration) = {
    Exit(App.run(config.arguments))
  }
}

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

case class Exit(val code: Int) extends xsbti.Exit
