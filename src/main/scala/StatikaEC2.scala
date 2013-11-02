package ohnosequences.statika.cli

import ohnosequences.awstools.ec2._
import ohnosequences.awstools.ec2.{Tag => Ec2Tag}

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
          case Some(a) => println("Instance address: "+a)
          case _ => println("Error: couldn't get instance address")
        }
        inst
      }
    }

    def applyAndWait(name: String, specs: InstanceSpecs, number: Int = 1): List[ec2.Instance] = {
      // TODO: run instances in parallel
      ec2.runInstances(number, specs) map { inst =>
        def status: String = inst.getTagValue("statika-status").getOrElse("...")

        val id = inst.getInstanceId()
        var previous: String = ""
        def printStatus(s: String) = println(name+" ("+id+"): "+s)

        inst.createTag(Ec2Tag("Name", name))
        printStatus("launched")

        while(status != "preparing") { Thread sleep 2000 }
        printStatus("url: "+inst.getPublicDNS().getOrElse("..."))

        while({val s = status; s != "failure" && s != "success"}) {
          val s = status
          if (s != previous) printStatus(s)
          previous = s
          Thread sleep 3000
        }
        printStatus(status)
        if (status == "success") Some(inst) else None
      } flatten
    }

  }

}
