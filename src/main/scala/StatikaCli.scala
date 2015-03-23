package ohnosequences.statika.cli


import org.rogach.scallop._
import buildinfo._
import java.io._
import scala.io._
import scala.sys.process._
import scala.language.reflectiveCalls


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
    validate (template) { t =>
      val parts = t.split("/").length
      if(parts == 2 || parts == 3) Right(Unit)
      else Left(s"Wrong format: '${t}'. Should be <org/repo[/version]>")
    }

    val branch = opt[String](
          default = Some("master")
        , descr = "Branch of the giter8 template"
        )

    val jsonFile = trailArg[String](
          descr = "Bundle configuration file(s) in JSON format"
        )
    validate (jsonFile) { f =>
        if (new File(f).exists) Right(Unit)
        else Left(s"File ${f} doesn't exists")
    }

  }

  val apply = new Subcommand("apply") {

    mainOptions = Seq(jar, bundle, dist, creds)

    val jar = opt[String](
          descr = "Jar file containing the distribution you are going to use"
        )
    validate (jar) { f =>
        if (new File(f).exists) Right(Unit)
        else Left(s"File ${f} doesn't exists")
    }

    val dist = opt[String](
          descr = "Full distribution object name"
        )

    val bundle = opt[String](
          descr = "Full bundle object name"
        )

    val creds = opt[String](
          descr = "Credentials file (with access key and secret key for Amazon AWS)"
        )
    validate (creds) { f =>
        if (new File(f).exists) Right(Unit)
        else Left(s"File ${f} doesn't exists")
    }

    val instType = opt[String](
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
    validate (number) { n =>
      if (n > 0) Right(Unit)
      else Left (s"Can't run ${n} instances in the real world")
    }

  }

}

object App {

  import org.json4s._
  import org.json4s.native.JsonMethods._

  import ohnosequences.awstools.ec2._
  import StatikaEC2._
  import BundleDescription._

  def main(args: Array[String]) {

    val config = AppConf(args)

    config.subcommand match {

      case Some(config.json) => { // generating prefilled json conf

        val jname = config.json.name() stripSuffix ".json"

        import org.json4s.native.Serialization
        import org.json4s.native.Serialization.write
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

        println(g8cmd.mkString("\n  "))

        g8cmd.!

      }

      case Some(config.apply) => { // applying bundle to an instance

        def interpret(cmd: String): String = 
          Seq("scala", "-cp", config.apply.jar(), "-e", cmd).!!

        val userscript = interpret(
            s"""print(${config.apply.dist()}.userScript(${config.apply.bundle()}))"""
          )

        val ami = interpret(
            s"""print(${config.apply.dist()}.ami.id)"""
          ).trim

        val specs = InstanceSpecs(
            instanceType = InstanceType.fromName(config.apply.instType()),
            amiId = ami,
            keyName = config.apply.keypair(),
            deviceMapping = Map(),
            userData = userscript,
            instanceProfile = Some(config.apply.profile())
          )

        println(s"""Launching instances:
          |type:        ${specs.instanceType}
          |ami:         ${specs.amiId}
          |keypair:     ${specs.keyName}
          |profile ARN: ${specs.instanceProfile.getOrElse("None")}
          |""".stripMargin)

        val ec2 = EC2.create(new File(config.apply.creds()))
        val instances = ec2.applyAndWait(config.apply.bundle().split("\\.").last, specs) 
        if (instances.length == config.apply.number()) System.exit(0)
        else System.exit(1)

      }

      case _ => { // a wrong subcommand
        config.printHelp()
        System.exit(1)
      }

    }
    
  }

}
