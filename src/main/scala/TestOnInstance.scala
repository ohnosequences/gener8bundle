package ohnosequences.statika.gener8bundle

import ohnosequences.awstools.ec2._
import com.decodified.scalassh._
import scala.sys.process._

object TestOnInstance {
  def test(
        instance: EC2#Instance
      , keypair: String
      , cmd: String
      , jname: String
      ): Int = {
    
    println("Instance ID: " + instance.getInstanceId())

    print("Instance initialization...")
    while (instance.getState() != "running") {
      Thread sleep 1000; print(".")
    }; println("ok!")

    val addr = instance.getPublicDNS().get
    println("Instance address: " + addr)
    
    print("Instance status ckecks...")
    while (instance.getStatus() != Some(InstanceStatus("ok","ok"))) {
      Thread sleep 1000; print(".")
    }; println("ok!")

    val sshConfig = HostConfig(
      login = PublicKeyLogin(
        user = "ec2-user"
      , keyfileLocations = keypair )
    , hostKeyVerifier = HostKeyVerifiers.DontVerify
    ) 

    val initialization: List[Either[String, Command]] = List(
      Left("Preparing necessary tools:")
    , Left("1. Installing sbt...")
    , Right("curl http://scalasbt.artifactoryonline.com/scalasbt/sbt-native-packages/org/scala-sbt/sbt//0.12.2/sbt.rpm > sbt.rpm")
    , Right("yum install -y sbt.rpm")

    , Left("2. Installing conscript...")
    , Right("curl https://raw.github.com/n8han/conscript/master/setup.sh | sh")
    , Right("/home/ec2-user/bin/cs --setup")

    , Left("3. Installing giter8...")
    , Right("/home/ec2-user/bin/cs n8han/giter8")

    // , Left("4. Installing gener8bundle...")
    // , Right("/home/ec2-user/bin/cs ohnosequences/gener8bundle")// -b feature/remote-testing")

    , Left("Tools are ready")

    , Left("Generating bundle for testing")
    , Right("/home/ec2-user/bin/" + cmd)
    , Right("cd " + jname)
    , Right("sbt run")
    )

    val result = SSH(addr, sshConfig) { client =>
      initialization.foldLeft("") { (acc,step) => step match {
        case Left(msg) => {
          println(msg)
          msg
        }
        case Right(cmd) => {
          println(cmd.command)
          client.exec(cmd) match {
            case Left(msg) => {
              println("Error: " + msg)
              msg
            }
            case Right(result) => {
              println(result.stdOutAsString())
              println(result.stdErrAsString())
              result.stdOutAsString()
            }
          }
        }
      } }
    }
    println(result)
    return 0
  }
}
