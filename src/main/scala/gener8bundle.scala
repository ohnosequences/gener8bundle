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
  , ami: String = "ami-c37474b7"
  , template: String = "ohnosequences/statika-bundle"
  , branch: String = "master"
  , jsons: List[String] = List()
  )

object App {

  import scala.sys.process._
  import scala.io._
  import java.io._
  import org.json4s._
  import org.json4s.native.JsonMethods._
  import BundleDescription._
  import TestOnInstance._
  import ohnosequences.awstools.ec2._

  /** Standard runnable class entrypoint */
  def main(args: Array[String]) {
    System.exit(run(args))
  }

  /** Shared by the launched version and the runnable version,
   * returns the process status code */
  def run(args: Array[String]): Int = {
    val argsParser = new scopt.immutable.OptionParser[Config]("gener8bundle", "0.7.3") {
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
        opt("i", "type", "Instance type (default is c1.medium)") {
          (v: String, c: Config) => c.copy(instanceType = v)
        },
        opt("a", "ami", "Amazon Machine Image (AMI) ID (default ami-c37474b7)") {
          (v: String, c: Config) => c.copy(ami = v)
        },
        opt("t", "template", "Bundle giter8 template from GitHub in format <org/repo[/version]> (default is ohnosequences/statika-bundle)") {
          (v: String, c: Config) => c.copy(template = v)
        },
        opt("b", "branch", "Branch of the giter8 template (default is master)") {
          (v: String, c: Config) => c.copy(branch = v)
        },
        arglist("<json-file>...", "Bundle configuration file(s) in JSON format") {
          (v: String, c: Config) => c.copy(jsons = v :: c.jsons)
        }
      )
    } 

    argsParser.parse(args, Config()) map { config =>

      if (config.prefill) {
        import org.json4s.JsonDSL._
        config.jsons map { j => 
          val name = j stripSuffix ".json"
          val bd = s"""{
    "name": "$name",            // String - name of the bundle
    "bundle_version": "0.1.0",  // Option[String] - initial version of bundle
    "tool_version": "",         // Option[String] - version of the tool, that is bundled
    "description": "",          // Option[String] - optional description
    "org": "ohnosequences",     // Option[String] - name of organization which is used in package and artifact names
    "scala_version": "2.10.0",  // Option[String] - version of Scala compiler
    "publish_private": "true",  // Boolean - if true, bundle will use private S3 buckets for publishing
    "dependencies": [           // List[BundleDependency] - optional list of dependencies, which are also json objects:
    //{
        //"name": ""            // String - name of dependency
        //"tool_version": ""    // Option[String] - it's tool version
        //"bundle_version": ""  // Option[String] - it's version itself
    //} , ...
    ]
}"""
          val file = new File(name+".json")
          if (file.exists) {
            println("Error: file "+ name +".json already exists")
            return 1
          } else Some(new PrintWriter(file)).foreach{p => p.write(bd); p.close}
        }
        return 0
      }

      implicit val formats = DefaultFormats

      // constructing giter8 commands for jsons
      val cmds: List[Seq[String]] = config.jsons map { file =>
        // reading file
        val jsonConf = parse(Source.fromFile(file).mkString)
        // parsing it
        val conf = jsonConf.extract[BundleDescription]
        // constructing g8 command with arguments
        "g8" +: config.template +: "-b" +: config.branch +: conf.toSeq
      }

      def err(msg: String): Int = {
        println(msg)
        argsParser.showUsage
        return 1
      }
      

      if (config.remotely) {
        if (config.credentials.isEmpty) 
          return err("Error: If you want to test bundle remotely, you need to provide credentials file with --credetntials option")
        if (config.keypair.isEmpty) 
          return err("Error: If you want to test bundle remotely, you need to provide keypair name with --keypair option")

        val ec2 = EC2.create(new File(config.credentials))
        TestOnInstance.runTestInstance(ec2, InstanceSpecs(
            instanceType = InstanceType.InstanceType(config.instanceType)
          , amiId = config.ami
          , keyName = config.keypair.split("/").last.takeWhile(_ != '.')
          ), config.keypair, cmds)

      } else {
        cmds.foldLeft(0){ (result, cmd) =>
          println(cmd)
          val r = cmd.!
          if (r == 0) result else r
        }
      }

    } getOrElse { return 1 } // if arguments were incorrect
  }
}
