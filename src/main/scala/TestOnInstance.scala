package ohnosequences.statika.gener8bundle

import ohnosequences.awstools.ec2._
import scala.sys.process._
// import Config
import java.io._

object TestOnInstance {
  def initScript(cmd: String, jname: String): String = s"""#!/bin/sh
cd /root

# redirecting output for logging
exec &> /root/log.txt

# sbt
curl http://scalasbt.artifactoryonline.com/scalasbt/sbt-native-packages/org/scala-sbt/sbt//0.12.2/sbt.rpm > sbt.rpm
yum install -y sbt.rpm &> /root/sbt.out

# conscript
curl https://raw.github.com/n8han/conscript/master/setup.sh | sh &> /root/cs.out
cs --setup &> /root/cs-setup.out

# giter8
cs n8han/giter8 &> /root/g8.out
cp /root/bin/g8 /bin/g8
chmod a+x /bin/g8

# gener8bundle script
# cs ohnosequences/gener8bundle &> /root/gener8bundle.out
# cp /root/bin/gener8bundle /bin/gener8bundle
# chmod a+x /bin/gener8bundle

touch /root/ready.out

# running g8
$cmd &> /root/g8-run.out
cd $jname
sbt run &> /root/sbt-run.out
"""

  def test(
        config: Config
      , cmd: String
      , jname: String
      ): Int = {
    
    val ec2 = EC2.create(new File(config.credentials))

    val instances = {
      val specs = InstanceSpecs(
          instanceType = InstanceType.InstanceType(config.instanceType)
        , amiId = config.ami
        , keyName = config.keypair.split("/").last.takeWhile(_ != '.')
        , userData = TestOnInstance.initScript(cmd, jname)
        )
      ec2.runInstances(1, specs)
    }

    if (instances.isEmpty) {
      println("Couldn't access an instance for testing")
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

    println(cmd)
    return 0
  }
}
