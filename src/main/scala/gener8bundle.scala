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

  def g8Args(m: Map[String, Any]): Seq[String] = {
    val conf = if (! m.contains("dependencies")) m
    else {
      val d = m("dependencies").asInstanceOf[List[String]]
      (m - "dependencies"
       ++ List(("depsSbt", depsSbt(d))
        ,("depsImport", depsImport(d))
        ,("depsHList", depsHList(d))))
    }
    conf.toSeq map {case (k,v) => "--" + k + "=" + v.toString.replaceAll(" ", "\\ ")}
  }
}

object Gener8Bundle extends App {

  import scala.sys.process._
  import scala.io.Source
  import scala.util.parsing.json.JSON._
  import Transformations._

  override def main(args: Array[String]) {

    if(args.length < 2)
    println("Usage: \n scala gen-bundle.scala <giter8 template address> <config_1.json> [... <config_n.json>]")
    else {
      val template = args(0)

      for (file <- args.tail) {
        val jsonConf: String = Source.fromFile(file).mkString

        jsonObj(new lexical.Scanner(jsonConf)) match {
          case NoSuccess(msg, _) => println("JSON parsing error: \n\t" + msg)
          case Success(m, _) => {
            val conf = resolveType(m).asInstanceOf[Map[String, Any]]
            val g8cmd = "g8" +: template +: g8Args(conf)

            println(g8cmd)
            g8cmd.!
          }
        }
      }
    }
  }
}
