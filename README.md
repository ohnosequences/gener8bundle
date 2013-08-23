## Statika CLI (Comman Line Interface)

This is a command line tool for
* generating [statika](https://github.com/ohnosequences/statika) bundles from a [template](https://github.com/ohnosequences/statika-bundle.g8) and a json-configuration.
* creating that prefilled json-configuration
* applying an existing bundle to a new EC2 instance


### JSON config

The configuration format is a json object which looks like this:

```json
{
  "bundle": <BundleEntity>,
  "sbtStatikaPlugin": <BundleEntity>,
  "dependencies":[
    <List[<BundleEntity>]>
  ]
}
```

So it consists of the elements:
* `bundle` is what you are going to create
* `sbtStatikaPlugin` is the sbt-statika plugin that you're going to use for it
* `dependencies` is a list of you bundle dependencies

`<BundleEntity>` is a json object of the following form:

```json
{
  "org": <String>,
  "name": <String>,
  "version": <String>,
  "objectName": <String>  // optional!
}
```

So it just takes the description of the bundle. And the `objectName` field is optional, so that if you don't fill it, it will be a transormed form of `name` (to be a proper Scala identifier).


#### Prefilled JSON

When you just start with bundle creation, you can just say

```bash
statika json  -o someOrg  foo
```

It will create `foo.json` file, prefilled with default values (`-o/--organization` paramater is optional — by default it's `ohnosequences`) and then you can open file and start adjusting it for your needs.


#### Example

Here is a nice example of a json config with dependencies:

```json
{
  "bundle":{
    "org":"era7",
    "name":"kaaa",
    "version":"0.1.0-SNAPSHOT",
    "objectName":"Kabaa"
  },
  "sbtStatikaPlugin":{
    "org":"ohnosequences",
    "name":"sbt-statika-ohnosequences",
    "version":"0.1.1"
  },
  "dependencies":[
    {
      "org":"ohnosequences",
      "name":"git",
      "version":"0.5.0"
    },{
      "org":"ohnosequences",
      "name":"ami-44939930",
      "version":"0.7.0",
      "objectName":"ami.AmazonLinuxAMIBundle"
    }
  ]
}
```

These are real bundles and if you save it to kaaa.json and run

```bash
statika generate kaaa.json
cd kaaa
sbt compile
```

It should just compile!



### Installation

To install the tool you need to install first [Conscript](https://github.com/n8han/conscript):

```bash
curl https://raw.github.com/n8han/conscript/master/setup.sh | sh
```

Then, you need to install [giter8](https://github.com/n8han/giter8) templating tool:

```bash
cs n8han/giter8
```

And finally the tool itself:

```bash
cs ohnosequences/statika-cli
```
