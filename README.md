## Statika CLI (Command Line Interface)

This is a command line tool for
* generating [statika](https://github.com/ohnosequences/statika) bundles from a [template](https://github.com/ohnosequences/statika-bundle.g8) and a json-configuration;
* applying an existing bundle to a new EC2 instance.


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


### Creating new bundles

When creating a new bundle you need to list it's dependencies and as it's not possible with a usual g8 template, _statika-cli_ offers the following solution:
* first, you create a json description of the bundle with it's name and the list of dependencies;
  + you can start from `statika json foo` which will generate a json description with the name filled;
  + then just edit it to add dependencies;
* now you call `statika generate foo.json` and get a project of your `foo` bundle.


#### JSON config

The configuration format is a json object which looks like this:

```json
{
  "bundle": <BundleEntity>,
  "dependencies":[
    <List[<BundleEntity>]>
  ]
}
```

So it consists of the elements:
* `bundle` is what you are going to create
* `dependencies` is a list of you bundle dependencies

`<BundleEntity>` is a json object of the following form:

```json
{
  "org": <String>,
  "name": <String>,
  "version": <String>,
  "objectName": <String>
}
```

So it just takes the description of the bundle. And the `objectName` field **is optional**, so that if you don't fill it, it will be a transformed `name` (to be a proper Scala identifier).

See `statika json --help` for available options.


#### Bundle generation

When you already have the json description ready, call

```
statika generate foo.json
```

and you will get the project in `foo` directory.

See `statika generate --help` for available options. Normally you are not interested in them, because you should use the default template.


### Applying a bundle

Assuming, that you have the bundle and the distribution containing it released, to apply this bundle you need several things:

* Distribution fat-jar (i.e. which contains all it's dependencies) file;
* Bundle and distribution fully-qualified object names (how to you refer to them in the scala code);
* Credentials for launching an instance.

Now, let's take for example an existing bundle and see how we can apply it. So the object names are

* distribution: `ohnosequences.statika.distributions.StatikaDistribution`
* bundle: `ohnosequences.statika.Velvet`

#### Getting distribution fat jar

When a distribution is released, a special fat-jar artifact is published. Distribution stores the address where it is published in it's metadata: 

```scala
> ohnosequences.statika.distributions.StatikaDistribution.metadata.artifactUrl
s3://releases.era7.com/ohnosequences/statika-distributions_2.10/0.8.0/statika-distributions_2.10-0.8.0-fat.jar
```

So, when you know this address, you can go to the bucket and download it. If you have AWS command line tools installed, you can do it with

```
aws s3 cp <this url> dist.jar
```

Now, let's assume that you have this fat jar saved in the `dist.jar` local file.

> Alternatively, check out distribution's [github releases page](https://github.com/ohnosequences/statika-distributions/releases/v0.8.0) — it may also contain this jar attached to the release, so that you don't need to search for it's address.

#### Apply command

The last thing that we need is a file of format

```
accessKey = ...
secretKey = ...
```

with your credentials. Let's call it `AwsCredentials.properties`.

Now you can just use `apply` command:

```
statika apply \
  --creds AwsCredentials.properties \
  --jar dist.jar \
  --dist ohnosequences.statika.distributions.StatikaDistribution \
  --bundle ohnosequences.statika.Velvet
```

(you can write it in one line wihtout `\` symbol).

Another alternative is to save options to a file `apply-velvet.opts`:

```
apply
--creds AwsCredentials.properties
--jar dist.jar
--dist ohnosequences.statika.distributions.StatikaDistribution
--bundle ohnosequences.statika.Velvet
```

and run 

```
statika @apply-velvet.opts
```

This way is more convenient, because this `opts` file is reusable and you can prepare such files for every bundle you apply oftenly.


#### Other options

See `statika apply --help` for all possible options. For example, you can choose instance type and the keypair to access the instance later by ssh. 

One important option is `--profile`: it should be an instance profile ARN, which corresponds to a role allowing this instance to access the S3 bucket with that distribution fat jar artifact. If you cannot use default role, ask your admin to create a new one.
