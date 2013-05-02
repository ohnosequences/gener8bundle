package ohnosequences.statika.gener8bundle

import ohnosequences.awstools.ec2._
import com.decodified.scalassh._

object TestOnInstance {
  def runTestInstance(ec2: EC2, instanceSpecs: InstanceSpecs, keypair: String, cmds: List[Seq[String]]): Int = {
    println("Running instance for testing...")
    // val instances = ec2.runInstances(1, instanceSpecs)
    // if (instances.isEmpty) return 1
    // val inst = instances.head

    val inst = ec2.getInstanceById("i-989568d5").get

    print("Waiting for instance initialization...")
    while (inst.getState() != "running") {
      Thread sleep 1000; print(".")
    }; println("ok!")

    print("Getting instance address...")
    while (inst.getPublicDNS() == None) {
      Thread sleep 1000; print(".")
    }; println("ok!")
    val addr = inst.getPublicDNS().get

    println("Address: " + addr)
    
    print("Waiting for instance status ckecks...")
    while (inst.getStatus() != Some(InstanceStatus("ok","ok"))) {
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

    , Left("4. Installing gener8bundle...")
    , Right("/home/ec2-user/bin/cs ohnosequences/gener8bundle")

    , Left("Tools are ready")
    )

    val result = SSH(addr, sshConfig) { client =>
      initialization.foldLeft("") { (acc,step) => step match {
        case Left(msg) => {
          println(msg)
          msg
        }
        case Right(cmd) => {
          println(cmd)
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
