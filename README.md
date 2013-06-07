This is a command line tool for
* generating [statika](https://github.com/ohnosequences/statika) bundles from a [template](https://github.com/ohnosequences/statika-bundle.g8) and a json-configuration.
* creating that prefilled json-configuration
* applying an existing bundle to a new EC2 instance

The configuration format is a json object which looks like this:

```json
{
    "name": "foo",
    "tool_version": "",
    "description": "Statika bundle for the foo tool",
    "org": "ohnosequences",
    "is_private": true,
    "ami": {
        "name": "ami-44939930",
        "tool_version": "2013.03",
        "bundle_version": "0.5.2"
    },
    "dependencies": []
}
```

* `name: String` — name of the bundle
* `tool_version: Option[String]` — version of the tool, that is bundled
* `description: Option[String]` — optional description
* `org: Option[String]` — name of organization which will be used in the package and the artifact names
* `is_private: Boolean` — if true, bundle will use private S3 buckets for publishing
* `ami: BundleDependency` — AMI bundle which will be used
* `dependencies: List[BundleDependency]` — optional list of dependencies, which have the same format as `ami`:
  + `name: String` — name of dependency
  + `tool_version: Option[String]` — it's tool version
  + `bundle_version: Option[String]` — it's version
