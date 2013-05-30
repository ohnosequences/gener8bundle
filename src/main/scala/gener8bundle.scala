package ohnosequences.statika.gener8bundle

// This script parses given json bundle configuration,
// constructs parameters for giter8 and calls it

/** The launched conscript entry point */
class App extends xsbti.AppMain {
  def run(config: xsbti.AppConfiguration) = {
    Exit(App.run(config.arguments))
  }
}

case class Exit(val code: Int) extends xsbti.Exit

case class Config(
    prefill: Boolean = false
  , remotely: Boolean = false
  , credentials: String = ""
  , keypair: String = ""
  , instanceType: String = "c1.medium"
  , ami: String = "ami-44939930"
  , template: String = "ohnosequences/statika-bundle"
  , branch: String = ""
  , json: String = ""
  )

object App {

  import scala.sys.process._
  import scala.io._
  import java.io._
  import org.json4s._
  import org.json4s.native.JsonMethods._
  import ConfigDescription._
  import TestOnInstance._
  import ohnosequences.awstools.ec2._

  /** Standard runnable class entrypoint */
  def main(args: Array[String]) {
    System.exit(run(args))
  }

  /** Shared by the launched version and the runnable version,
   * returns the process status code */
  def run(args: Array[String]): Int = {
    val argsParser = new scopt.immutable.OptionParser[Config]("gener8bundle", "0.10.0") {
      def options = Seq(
        flag("p", "prefill", "Creates json configs with given names prefilled with default values") {
          (c: Config) => c.copy(prefill = true)
        },
        flag("r", "remotely", "Test bundle configuration on Amazon EC2 instance (default off)") {
          (c: Config) => c.copy(remotely = true)
        },
        opt("c", "credentials", "Credentials file (with access key and secret key for Amazon AWS") {
          (v: String, c: Config) => c.copy(credentials = v)
        },
        opt("k", "keypair", "Keypair for connecting to the test EC2 instance") {
          (v: String, c: Config) => c.copy(keypair = v)
        },
        opt("type", "Instance type (default is c1.medium)") {
          (v: String, c: Config) => c.copy(instanceType = v)
        },
        opt("a", "ami", "Amazon Machine Image (AMI) ID (default ami-44939930)") {
          (v: String, c: Config) => c.copy(ami = v)
        },
        opt("t", "template", "Bundle giter8 template from GitHub in format <org/repo[/version]> (default is ohnosequences/statika-bundle)") {
          (v: String, c: Config) => c.copy(template = v)
        },
        opt("b", "branch", "Branch of the giter8 template (default is master)") {
          (v: String, c: Config) => c.copy(branch = v)
        },
        arg("<json-file>", "Bundle configuration file(s) in JSON format") {
          (v: String, c: Config) => c.copy(json = v)
        }
      )
    } 

    argsParser.parse(args, Config()) map { config =>

      val jname = config.json stripSuffix ".json"
      if (config.prefill) {
        import org.json4s.JsonDSL._
        val bd = s"""{
    "name": "$jname",
    "tool_version": "",
    "description": "New bundle for $jname tool",
    "org": "ohnosequences",
    "is_private": true,
    "ami": {
        "name": "ami-44939930",
        "tool_version": "2013.03",
        "bundle_version": "0.5.2"
    },
    "dependencies": []
}"""
        val file = new File(jname+".json")
        if (file.exists) {
          println("Error: file "+ jname +".json already exists")
          return 1
        } else Some(new PrintWriter(file)).foreach{p => p.write(bd); p.close}
        return 0
      }

      // constructing giter8 command for json

      implicit val formats = DefaultFormats
      // reading file
      val j = Source.fromFile(config.json).mkString
      // parsing it
      val jsonConf = parse(j)
      val bd = jsonConf.extract[BundleDescription]

      val g8args: Seq[String] =
        Seq(config.template) ++
        (if (config.branch.isEmpty) Seq() else Seq("-b", config.branch)) ++
        bd.toSeq

      val g8cmd = "g8" +: g8args

      println(g8cmd.mkString(" \\\n  "))

      def err(msg: String): Int = {
        println(msg)
        argsParser.showUsage
        return 1
      }

      g8cmd.!

      // if (!config.remotely) g8cmd.!
      // else {
      //   if (config.credentials.isEmpty) 
      //     return err("Error: If you want to test bundle remotely, you need to provide credentials file with --credetntials option")
      //   if (config.keypair.isEmpty) 
      //     return err("Error: If you want to test bundle remotely, you need to provide keypair name with --keypair option")

      //   TestOnInstance.test(
      //     config
      //   , g8cmd.map("'"+_+"'").mkString(" ")
      //   , jname
      //   )
      // }

    } getOrElse { return 1 } // if arguments were incorrect
  }
}
