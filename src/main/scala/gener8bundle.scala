package ohnosequences.statica.gener8bundle

// This script parses given json bundle configuration,
// constructs parameters for giter8 and calls it

object Transformations {

  def startCase(s: String) = s.toLowerCase.split(" ").map(_.capitalize).mkString(" ")
  def wordOnly(s: String) = s.replaceAll("""\W""", "")
  def upperCamel(s: String) = wordOnly(startCase(s))
  def hyphenate(s: String) = s.replaceAll("""\s+""", "-")
  def normalize(s: String) = hyphenate(s.toLowerCase)

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
}

case class BundleDescription(
    name: String
  , version: Option[String]
  , dependencies: List[String]
  ) {
  def toSeq: Seq[String] = {
    def format(k: String, v: String) = "--" + k + "=" + v.toString.replaceAll(" ", "\\ ")
    def opt[A](k: String, v: Option[A]) = v.toList.map((k, _))

    import Transformations._

    (Seq(("name", name)) ++ opt("version", version) ++
      ( if (dependencies.isEmpty) Seq()
        else Seq(("depsSbt", depsSbt(dependencies)),
                 ("depsImport", depsImport(dependencies)),
                 ("depsHList", depsHList(dependencies)))
      )
    ) map {case (k,v) => format(k,v)}
  }
}

object BundleJsonProtocol extends spray.json.DefaultJsonProtocol {
  implicit val bundleFormat = jsonFormat3(BundleDescription)
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
  import scala.util.parsing.json.JSON._
  import Transformations._
  import spray.json._
  import BundleJsonProtocol._


  /** Shared by the launched version and the runnable version,
   * returns the process status code */
  def run(args: Array[String]): Int = {
    if(args.length < 2) {
      println("Usage: gener8bundle <giter8 template address> <config_1.json> [... <config_n.json>]")
      1
    }
    else {
      val template = if (args(0) == "-") "ohnosequences/statica-bundle" else args(0)

      args.tail.foldLeft(0){ (result, file) =>
        // reading json
        val jsonConf = Source.fromFile(file).mkString.asJson
        // parsing it
        val conf = jsonConf.convertTo[BundleDescription]
        // constructing g8 command with arguments
        val g8cmd = "g8" +: template +: conf.toSeq

        println(g8cmd)
        // running it
        val r = g8cmd.!
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
