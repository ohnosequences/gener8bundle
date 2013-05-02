package ohnosequences.statika.gener8bundle

import ohnosequences.awstools.ec2._
import com.decodified.scalassh._

object TestOnInstance {
  def runTestInstance(ec2: EC2, instanceSpecs: InstanceSpecs, keypair: String, cmds: List[Seq[String]]): Int = {
    println("Running instance for testing...")
    val instances = ec2.runInstances(1, instanceSpecs)
    if (instances.isEmpty) return 1
    val inst = instances.head

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
        user = "ec2-user",
        keyfileLocations = keypair
      )
    )
    SSH(addr, sshConfig) { client => 
      println("Preparing necessary tools:")
      println("1. Installing sbt...")
      client.exec("curl http://scalasbt.artifactoryonline.com/scalasbt/sbt-native-packages/org/scala-sbt/sbt//0.12.2/sbt.rpm > sbt.rpm")
      client.exec("yum install -y sbt.rpm")

      println("2. Installing conscript...")
      client.exec("curl https://raw.github.com/n8han/conscript/master/setup.sh | sh")
      client.exec("cs --setup")

      println("3. Installing giter8...")
      client.exec("cs n8han/giter8")

      println("4. Installing gener8bundle...")
      client.exec("cs ohnosequences/gener8bundle")

      println("Tools are ready")

    }
    return 0
  }
}
