package ohnosequences.statika.cli

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
import java.io._
import scala.io._
import scala.sys.process._


case class AppConf(arguments: Seq[String]) extends ScallopConf(arguments) {

  version(s"${BuildInfo.name} ${BuildInfo.version}")

  val json = new Subcommand("json") {

    val organization = opt[String](
          default = Some("ohnosequences")
        , descr = "Organization for the bundle"
        )

    val name = trailArg[String](
          descr = "Creates json config file with given name prefilled with default values"
        )
    validate (name) { n =>
        val file = new File(n.stripSuffix(".json")+".json")
        if (file.exists) Left(s"File ${n}.json already exists")
        else Right(Unit)
    }

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

    mainOptions = Seq(fatJar, bundleObject, distObject, credentials)

    val fatJar = opt[String](
          descr = "Fat jar file of the distribution you are going to use"
        )

    val distObject = opt[String](
          descr = "Distribution object name"
        )

    val bundleObject = opt[String](
          descr = "Bundle object name"
        )

    val credentials = opt[String](
          descr = "Credentials file (with access key and secret key for Amazon AWS)"
        )
    // TODO: add validation of file existence

    val instanceType = opt[String](
          descr = "Instance type"
        , default = Some("c1.medium")
        )

    val keypair = opt[String](
          descr = "Name of keypair for the later ssh access to the launched instance"
        , default = Some("statika-launcher")
        )

    val profile = opt[String](
          descr = "Instance profile (role) to access private dependencies (if any)"
        , default = Some("arn:aws:iam::857948138625:instance-profile/statika-private-resolver")
        )

    val number = opt[Int](
          descr = "Number of instances to launch"
        , default = Some(1)
        )

  }

}

object App {

  import org.json4s._
  import org.json4s.native.JsonMethods._

  import ohnosequences.awstools.ec2._
  import StatikaEC2._
  import BundleDescription._

  /* Standard runnable class entrypoint */
  def main(args: Array[String]) {
    System.exit(run(args))
  }

  /*  Shared by the launched version and the runnable version,
      returns the process status code
  */
  def run(args: Array[String]): Int = {

    val config = AppConf(args)

    config.subcommand match {

      case Some(config.json) => { // generating prefilled json conf

        val jname = config.json.name() stripSuffix ".json"

        import org.json4s.native.Serialization
        import org.json4s.native.Serialization.{read, write}
        implicit val formats = Serialization.formats(NoTypeHints)

        val o = config.json.organization()
        val json = write(DescriptionFormat(BundleEntity(o, jname, "0.1.0-SNAPSHOT")))
        val text = pretty(render(parse(json)))

        Seq("echo", text) #> new File(jname+".json") !

      }

      case Some(config.generate) => { // constructing giter8 command to create a bundle

        implicit val formats = DefaultFormats
        // reading file
        val j = Source.fromFile(config.generate.jsonFile()).mkString
        // parsing it
        val jsonConf = parse(j)
        val bd = jsonConf.extract[DescriptionFormat]

        val g8args: Seq[String] =
          config.generate.template() +: 
          "-b" +: config.generate.branch() +:
          bd.toSeq

        val g8cmd = "g8" +: g8args

        println(g8cmd.mkString(" \\\n  "))

        g8cmd.!

      }

      case Some(config.apply) => { // applying bundle to an instance

        def interpret(cmd: String): String = 
          Seq("scala", "-cp", config.apply.fatJar(), "-e", cmd).!!

        val userscript = interpret(
            s"""print(${config.apply.distObject()}.userScript(${config.apply.bundleObject()}))"""
          )

        val ami = interpret(
            s"""print(${config.apply.distObject()}.ami.id)"""
          ).trim

        val specs = InstanceSpecs(
            instanceType = InstanceType.InstanceType(config.apply.instanceType())
          , amiId = ami
          , keyName = config.apply.keypair()
          , deviceMapping = Map()
          , userData = userscript
          , instanceProfileARN = Some(config.apply.profile())
          )

        println(s"""Launching instances:
          |type:        ${specs.instanceType}
          |ami:         ${specs.amiId}
          |keypair:     ${specs.keyName}
          |profile ARN: ${specs.instanceProfileARN.getOrElse("None")}
          |""".stripMargin)

        val ec2 = EC2.create(new File(config.apply.credentials()))
        val instances = ec2.applyAndWait(config.apply.bundleObject().split("\\.").last, specs) 
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
