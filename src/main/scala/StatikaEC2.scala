package ohnosequences.statika.cli

import ohnosequences.awstools.ec2._

object StatikaEC2 {

  implicit class StatikaEC2Tools(val ec2: EC2) {

    def waitForInit(instance: ec2.Instance) {
      print("Instance initialization...")
      while (instance.getState() != "running") {
        Thread sleep 1000; print(".")
      }; println("ok!")

      print("Instance status ckecks...")
      while (instance.getStatus() != Some(InstanceStatus("ok","ok"))) {
        Thread sleep 1000; print(".")
      }; println("ok!")
    }

    def runInstancesAndWait(number: Int = 1, specs: InstanceSpecs): List[ec2.Instance] = {
      ec2.runInstances(number, specs) map { inst =>
        println("Launched an instance with id: " + inst.getInstanceId())
        waitForInit(inst)
        val addr = inst.getPublicDNS()
        addr match {
          case Some(a) => {
            println("Instance address: "+a)
            println("Command to connect to the instance:")
            println("ssh -i "+specs.keyName+" ec2-user@+"+a)
          }
          case _ => println("Error: couldn't get instance address")
        }
        inst
      }
    }

  }

}
