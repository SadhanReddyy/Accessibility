# Getting Started

This package contains the source code for your application.
It contains several built-in goodies: RDE for local development and SAMToolkit to deploy the ECS cluster and service to your own AWS account.

## How To Run Your Application Locally using RDE

We've configured this package to enable you to test your application locally [with Rapid Dev Environment (RDE)](https://builderhub.corp.amazon.com/tools/rde/index.html).  To run your application, simply run the command:

```sh
rde workflow run
```

When you do this, RDE, will rebuild any necessary packages, clean up any old resources, and build your Docker image based on the Dockerfile in your AWSConsoleAccessibilityImageBuild package (located at configuration/Dockerfile).  Once RDE has built the Docker image, it will launch it and a few other Docker containers as part of [a Docker Stack](https://docs.docker.com/get-started/part5/).

You can see these containers by running the "docker ps" command:

```sh
dev-dsk-username % docker ps
CONTAINER ID        IMAGE                                                       COMMAND                   CREATED             STATUS              PORTS                      NAMES
45ff30722336        011465881737.dkr.ecr.us-west-2.amazonaws.com/rde-endpoint   "/bin/sh -c 'trap \"/b"   21 minutes ago      Up 21 minutes       127.0.0.1:8080->8080/tcp   my.service.endpoint.com
e473c1272eb6        AWSConsoleAccessibility                                  "/bin/sh -c 'sleep 5;"    21 minutes ago      Up 21 minutes       8080/tcp, 8443/tcp         AWSConsoleAccessibility
d011cb7f1d8d        011465881737.dkr.ecr.us-west-2.amazonaws.com/rde-base       "/home/deceneu/binary"    22 minutes ago      Up 22 minutes                                  AWSConsoleAccessibilityStack.Ec2MetadataProxy
```

The first container (named "my.service.endpoint.com") provides a port on your local machine (127.0.0.1:8080) that the application inside the Docker internal network can listen for and respond to traffic from your development machine.

The second container (named "AWSConsoleAccessibility") is your application itself.

The third container (named "AWSConsoleAccessibilityStack.Ec2MetadataProxy") provides a fake EC2 metadata service to your running application.  The values here can [be overridden as explained in the RDE wiki](https://w.amazon.com/index.php/RDE/How_do_I#Overwrite_the_values_returned_by_the_EC2_metadata_service_from_the_Personal_Stack_.28i.e._change_the_AWS_account_region.29).  The file we provide for this is at configuration/rde_ec2_metadata.yaml.


## How To Test Your Application using RDE

We provide some default tests. Before running these test, you need to set base image OS to AL2 because the IMDSv2 call in test `ec2md-smoke-test`
is only supported on AL2 for now:
```sh
rde stack restart --os AL2
```

Then you can run tests with the command:

```sh
rde workflow run validate
```

These are defined in your definition.yaml file.  They're a collection of [RDE Steps](https://w.amazon.com/index.php/RDE/Definition#steps) that do stuff like make sure your EC2 Metadata Service is running, that your application container is running, and that it's possible to hit it with network call both inside the Docker internal network and from your development machine as well.

If you want to hit it manually, you can do so as normal:

```sh
dev-dsk-username % curl 127.0.0.1:8080/ping
healthy
```

You can also shell into the image using the "docker exec" command, at which point you're free to see what all got stuck in your image:

```
dev-dsk-username % docker exec -it AWSConsoleAccessibility bash
bash-4.2# pwd
/
bash-4.2# ls
bin  boot  dev  etc  home  lib  lib64  local  media  mnt  opt  proc  root  run  sbin  srv  sys  tmp  usr  var
bash-4.2#
```

## How The Dockerfile Entrypoint Command Works

The script we run to start your application ("entry_point.sh") is in this package's configuration/bin/ directory, and you are free to replace it with anything else you'd like.  However, there are some benefits to understanding the system we've provided and working with it rather than discarding it.

By default, entry_point.sh invokes [a script provided by the ApolloShimOpConfigHelpers package](https://code.amazon.com/packages/ApolloShimOpConfigHelpers/blobs/88f29d0d13fcbb6a207b0ec1e8ac90038883b623/--/bin/bones_run_apollo_shim.sh#L9), "bones_run_apollo_shim.sh".  This script performs all the actions required to set up your Apollo-like directory structure inside the Docker container, runs ApolloCmd scripts, etc.

Notice that in the "entry_point.sh" script, we pass an argument to the "bones_run_apollo_shim.sh":

```sh
exec /opt/amazon/bin/bones_run_apollo_shim.sh --script bin/run-service.sh
```

 This tells the Apollo Shim setup scripts that after they finish setting up your fake Apollo environment, they should run a script located in the Apollo Environment Root called "bin/run-service.sh".  This is the script that actually launches the Coral Stack inside the Docker container, and is currently being provided by [the Cloud9ApolloJavaWrapperGenerator package](https://code.amazon.com/packages/Cloud9JavaWrapperGenerator), which this package declares a dependency on.

Altogether, what this means is that you have some choices here.

1. If you want to do your own thing and operate with as light a footprint as possible, you can skip creating a bunch of junk in your Docker container by updating your "entry_point.sh" script not to call the Apollo Shim Setup scripts.
2. If you want to provide your own custom startup script while still having all of the Apollo Shim Setup stuff executed, you just need to update your "entry_point.sh" script to pass a different script into the Apollo Shim Setup scripts and make sure that script ends up in the runtime-closure of your application

## Troubleshooting Issues

### Environment Setup

Before you can use RDE/Docker for locally development, you'll obviously need to install them.  You can find a guide [to installing both of these in the RDE docs](https://builderhub.corp.amazon.com/docs/rde/cli-guide/setup.html).

### Docker Fundamentals

If you haven't played around with Docker before, we recommend you take a look at the Docker documentation.  Specific articles you should take a look at:

* [The Docker introductory tutorial](https://docs.docker.com/get-started/)
* [The Docker best practices guide](https://docs.docker.com/develop/dev-best-practices/)
* [The Dockerfile reference](https://docs.docker.com/engine/reference/builder/)

### ECS Metadata Service Support

RDE now provides a simulated version of the ECS Metadata Service ([read more here](https://builderhub.corp.amazon.com/blog/rde-now-has-ecs-local-endpoints-support-for-container-applications/)).

### Application Spinnup Issues

If you've never worked with Docker before, it can be mysterious how your application starts up inside the container and therefore difficult to figure out why it's failing to start up.  To explain this, we first need to go over how [a Docker build](https://docs.docker.com/engine/reference/commandline/build/) works.

Your [Dockerfile specifies a series of commands](https://docs.docker.com/engine/reference/builder/) that the Docker build will run in order to construct your Docker image.  The first command that runs is usually the "FROM" command, which specifies the base image on top of which to add your own changes (Amazon Linux, by default).  Every other command is run as if it were the "root" user inside of that image.  By default, the image will only contain the files defined by your base image.  If you want stuff from your runtime closure to show up in it, you'll need to use [the Dockerfile COPY command](https://docs.docker.com/engine/reference/builder/#copy).

The final command in your Dockerfile is typically going to be the entrypoint to your application.  That is, it will be the command that Docker runs to start up your application.  Your Docker container will stay alive as long as the process that the entrypoint command runs is alive, and the Docker container will spin down when that process exits.  Logs emitted by that process to stdout/stderr should show up in your terminal window, and you can use these messages to troubleshoot why your application is failing to spin up.  Logs emitted to files on "disk" inside your container do not get forwarded to the Docker logging facility by default (you can [read more about logging with Docker here](https://docs.docker.com/config/containers/logging/)).  It is [a Docker best practice to for each container to have a single running process](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/#decouple-applications) so you should generally consider it pathological to have your application configured to expect some other process to take logs you've written to disk and send them elsewhere (Timber, CloudWatch Logs, etc).

While RDE does print all the logs from the Docker Build steps it performs as part of creating your image, it does not print to stdout the logs from your entry point command.  That is, the command "rde workflow run" will finish and return an exit code of "0" whether your application started up in its container or not.  This is because there's no way for the RDE code to know when/if your entrypoint command will succeed or exit itself (e.g. [the Halting Problem](https://en.wikipedia.org/wiki/Halting_problem)).

In order to see the logs from your application's spinnup command, you can run the command:

```
docker logs AWSConsoleAccessibility
```

The RDE docs [discuss this issue in greater depth](https://w.amazon.com/index.php/RDE/Tutorial/Container_Applications#Why_isn.27t_RDE_printing_the_startup_logs.2Fexit_code_of_my_container_startup_logic).

If the logs are not enough, you can also do a workaround to make your Docker image run, shell into it, and poke around to see what's going on.  The easiest way to do this is to change your Dockerfile entrypoint command to an infinite loop:

```
while :; do echo 'Hit CTRL+C'; sleep 1; done
```

Afterwards, re-run "rde workflow run" and shell into your Docker container with the "docker exec" command.
