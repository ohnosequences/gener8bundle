## Statika CLI (Command Line Interface)

This is a command line tool for launching EC2 instances with Statika bundles.


### Installation

To install the tool you need to download the `statika.properties` file and do

```bash
sbt @./statika.properties
```

It should install the tool and display its version and help message


### Usage

Assuming, that you have the bundle and the distribution containing it released, to apply this bundle you need several things:

* Distribution fat-jar (i.e. which contains all it's dependencies) file;
* Bundle and distribution fully-qualified object names (how to you refer to them in the scala code);
> @marina-manrique: where and how I get these fully qualified names?
* Credentials for launching an instance.

Now, let's take for example an existing bundle and see how we can apply it. So the object names are

* distribution: `ohnosequences.statika.distributions.AmazonLinux`
* bundle: `ohnosequences.statika.Velvet`


#### Getting distribution fat jar

When a distribution is released, a special fat-jar artifact is published. Distribution stores the address where it is published in it's metadata:

```scala
> ohnosequences.statika.distributions.AmazonLinux.metadata.artifactUrl
s3://releases.era7.com/ohnosequences/statika-distributions_2.10/0.7.0/statika-distributions_2.10-0.7.0-fat.jar
```

So, when you know this address, you can go to the bucket and download it. If you have AWS command line tools installed, you can do it with

```
aws s3 cp <this url> dist.jar
```

Now, let's assume that you have this fat jar saved in the `dist.jar` local file.

> @marina-manrique: It's not clear to me where or how I can get this fat jar, in this example it's clear because it's written `s3://releases.era7.com/ohnosequences/statika-distributions_2.10/0.7.0/statika-distributions_2.10-0.7.0-fat.jar` but in general it is not. I don't know either how to use this command for getting the metadata ` ohnosequences.statika.distributions.AmazonLinux.metadata.artifactUrl`

> Alternatively, check out distribution's [github releases page](https://github.com/ohnosequences/statika-distributions/releases/v0.7.0) — it may also contain this jar attached to the release, so that you don't need to search for its address.


#### Apply command

> I'm missing the option on applying bundles to spot requests, also the instance storage (this is already discussed with @eparejatobes)

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
  --dist ohnosequences.statika.distributions.AmazonLinux \
  --bundle ohnosequences.statika.Velvet
```

(you can write it in one line wihtout `\` symbol).

Another alternative is to save options to a file `apply-velvet.opts`:

```
apply
--creds AwsCredentials.properties
--jar dist.jar
--dist ohnosequences.statika.distributions.AmazonLinux
--bundle ohnosequences.statika.Velvet
```

and run

```
statika @apply-velvet.opts
```

This way is more convenient, because this `opts` file is reusable and you can prepare such files for every bundle you apply often.


### Other options

See `statika apply --help` for all possible options. For example, you can choose instance type and the keypair to access the instance later by ssh.

One important option is `--profile`: it should be an instance profile ARN, which corresponds to a role allowing this instance to access the S3 bucket with that distribution fat jar artifact. If you cannot use default role, ask your admin to create a new one.
