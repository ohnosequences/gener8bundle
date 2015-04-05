package ohnosequences.statika.cli


import org.rogach.scallop._
import buildinfo._
import java.io._
import scala.io._
import scala.sys.process._
import scala.language.reflectiveCalls


case class AppConf(arguments: Seq[String]) extends ScallopConf(arguments) {

  version(s"${BuildInfo.name} ${BuildInfo.version}")

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

  import ohnosequences.awstools.ec2._
  import StatikaEC2._

  def main(args: Array[String]) {

    val config = AppConf(args)

    config.subcommand match {

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
