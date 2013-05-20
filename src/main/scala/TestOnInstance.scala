package ohnosequences.statika.gener8bundle

import ohnosequences.awstools.ec2._
import scala.sys.process._
import java.io._

object TestOnInstance {

  def test(credentialsFile: String, amiId: String, userscript: String): Int = {
    
    val ec2 = EC2.create(new File(credentialsFile))

    val instances = {
      val specs = InstanceSpecs(
          instanceType = InstanceType.InstanceType("c1.medium")
        , amiId = amiId
        , keyName = "statika-launcher"
        , deviceMapping = Map()
        , userData = userscript
        , instanceProfileARN = Some("arn:aws:iam::857948138625:instance-profile/statika-tester")
        )
      ec2.runInstances(1, specs)
    }

    if (instances.isEmpty) {
      println("Couldn't access instance for testing")
      return 1
    }
    val instance = instances.head

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

    return 0
  }

}
