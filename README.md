This is a tool for generating [statika](https://github.com/ohnosequences/statika) bundles from a [template](https://github.com/ohnosequences/statika-bundle.g8) and a json-configuration for pre-filling it's placeholders.

```
Usage: gener8bundle [options] <json-file>...

  -r | --remotely
        Test bundle configuration on Amazon EC2 instance (default off)
  -c <value> | --credentials <value>
        Credentials file (with access key and secret key for Amazon AWS
  -k <value> | --keypair <value>
        Keypair for connecting to the test EC2 instance
  -i <value> | --type <value>
        Instance type (default is c1.medium)
  -a <value> | --ami <value>
        Amazon Machine Image (AMI) ID (default ami-c37474b7)
  -t <value> | --template <value>
        Bundle giter8 template from GitHub in format <org/repo[/version]> (default is ohnosequences/statika-bundle)
  -b <value> | --branch <value>
        Branch of the giter8 template (default is master)
  <json-file>...
        Bundle configuration file(s) in JSON format
```

The configuration format is a json object with the following possible fields:

* `name: String` — name of the bundle
* `bundle_version: Option[String]` — initial version of bundle (default is `0.1.0`)
* `tool_version: Option[String]` — version of the tool, that is bundled (default is empty)
* `description: Option[String]` — optional description (default is empty)
* `org: Option[String]` — name of organization which is used in package and artifact names (default is `ohnosequences`)
* `scala_version: Option[String]` — version of Scala compiler (default is `2.10.0`)
* `publish_private: Boolean` — if true, bundle will use private S3 buckets for publishing
* `dependencies: List[BundleDependency]` — optional list of dependencies, which are also json objects:
  + `name: String` — name of dependency
  + `tool_version: Option[String]` — it's tool version
  + `bundle_version: Option[String]` — it's version itself

only `name` and `publish_private` are essential, others have default values.
