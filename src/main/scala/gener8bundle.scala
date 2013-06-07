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


import org.rogach.scallop._
import buildinfo._

case class AppConf(arguments: Seq[String]) extends ScallopConf(arguments) {

  version("%s %s" format (BuildInfo.name, BuildInfo.version))

  val json = new Subcommand("json") {

    val name = trailArg[String](
          descr = "Creates json config file with given name prefilled with default values"
        )

  }

  val generate = new Subcommand("generate") {

    val template = opt[String](
          default = Some("ohnosequences/statika-bundle")
        , descr = "Bundle giter8 template from GitHub in the format <org/repo[/version]>"
        )
    // TODO: add validation of the format

    val branch = opt[String](
          default = Some("master")
        , descr = "Branch of the giter8 template"
        )

    val jsonFile = trailArg[String](
          descr = "Bundle configuration file(s) in JSON format"
        )
    // TODO: add validation of file existence

  }

  val apply = new Subcommand("apply") {

    mainOptions = Seq(name, artifact, artifactVersion, credentials)

    val name = opt[String](
          descr = "Bundle object name"
        )

    val artifact = opt[String](
          descr = "Bundle artifact name"
        )

    val artifactVersion = opt[String](
          descr = "Bundle version"
        )

    val credentials = opt[String](
          descr = "Credentials file (with access key and secret key for Amazon AWS)"
        )
    // TODO: add validation of file existence

    val instanceType = opt[String](
          descr = "Instance type"
        , default = Some("c1.medium")
        )

    val ami = opt[String](
          descr = "Amazon Machine Image (AMI) ID"
        , default = Some("ami-44939930")
        )
    // TODO: add validation of the format

    val keypair = opt[String](
          descr = "Name of keypair for the later ssh access"
        , default = Some("statika-launcher")
        )

    val instanceProfile = opt[String](
          descr = "Instance profile (for roles)"
        , default = Some("arn:aws:iam::857948138625:instance-profile/statika-tester")
        )

    val number = opt[Int](
          descr = "Number of instances to launch"
        , default = Some(1)
        )

  }

}

object App {

  import java.io._
  import scala.io._
  import scala.sys.process._

  import org.json4s._
  import org.json4s.native.JsonMethods._

  import ohnosequences.awstools.ec2._
  import StatikaEC2._
  import ConfigDescription._

  /** Standard runnable class entrypoint */
  def main(args: Array[String]) {
    System.exit(run(args))
  }

  /** Shared by the launched version and the runnable version,
   * returns the process status code */
  def run(args: Array[String]): Int = {

    val config = AppConf(args)

    config.subcommand match {

      case Some(config.json) => { // generating prefilled json conf

        val jname = config.json.name() stripSuffix ".json"
        import org.json4s.JsonDSL._
        val bd = s"""{
    "name": "$jname",
    "tool_version": "",
    "description": "Statika bundle for the $jname tool",
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

      case Some(config.generate) => { // constructing giter8 to create a bundle

        implicit val formats = DefaultFormats
        // reading file
        val j = Source.fromFile(config.generate.jsonFile()).mkString
        // parsing it
        val jsonConf = parse(j)
        val bd = jsonConf.extract[BundleDescription]

        val g8args: Seq[String] =
          config.generate.template() +: 
          "-b" +: config.generate.branch() +:
          bd.toSeq

        val g8cmd = "g8" +: g8args

        println(g8cmd.mkString(" \\\n  "))

        g8cmd.!

      }

      case Some(config.apply) => { // applying bundle to an instance


        println("\n -- Constructing giter8 command -- \n")

        val g8cmd = Seq("g8", "ohnosequences/statika-bundle.g8"
            , "-b", "bundle-user-script"
            , "--artifact=" + config.apply.artifact()
            , "--class_name=" + config.apply.name()
            , "--version=" + config.apply.artifactVersion()
            )
        println(g8cmd.mkString("  "))


        println("\n -- Running giter8 -- \n")

        val g8result = g8cmd.!
        if (g8result != 0) return g8result

        // Adding method to run commands from a given path
        implicit class PBAt(cmd: Seq[String]) {
          implicit def @@(path: String): ProcessBuilder =
            Process(cmd, new java.io.File(path), "" -> "")
        }


        println("\n -- Generating user-script -- \n")

        val buildResult = (Seq("sbt", "start-script") @@ "bundle-user-script").!
        if (buildResult != 0) return buildResult

        val userscript = (Seq("./target/start") @@ "bundle-user-script").!!


        println("\n -- Launching instances -- \n")

        val ec2 = EC2.create(new File(config.apply.credentials()))

        val specs = InstanceSpecs(
            instanceType = InstanceType.InstanceType(config.apply.instanceType())
          , amiId = config.apply.ami()
          , keyName = config.apply.keypair()
          , deviceMapping = Map()
          , userData = userscript
          , instanceProfileARN = Some(config.apply.instanceProfile())
          )

        val instances = ec2.runInstancesAndWait(config.apply.number(), specs)

        if (instances.length == config.apply.number()) return 0
        else return 1

      }

      case _ => { // a wrong subcommand
        config.printHelp()
        return 1
      }

    }
    
  }

}
