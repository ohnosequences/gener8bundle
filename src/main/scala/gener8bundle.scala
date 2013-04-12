package ohnosequences.statica.gener8bundle

// This script parses given json bundle configuration,
// constructs parameters for giter8 and calls it

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
