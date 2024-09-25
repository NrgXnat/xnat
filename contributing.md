# Contributing to the XNAT development #

This is a quick-start guide to contributing to the open-source development of XNAT.
We will guide you though installing the necessary dependencies, setting up a development
environment, and building XNAT.

# Requirements #

You will require:

- a fork of the `xnat-web` repository, cloned to your local machine
- an IDE the supports Java, such as [IntelliJ](https://www.jetbrains.com/idea)
  or [VS Code](https://code.visualstudio.com) with the
  [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
- Java 8 JDK
- Maven
- Gradle

Note, you will need around 4 GB of RAM to build XNAT.

# Contribution workflow #

Steps for contributing to XNAT:

- create a fork of this repository and to your local machine
- create a feature branch to work on
- push your changes to your fork and submit a pull request to
  merge to changes into `xnat-web`

## Fork the xnat-web repository ##

Create a fork of the `xnat-web` repository at
https://bitbucket.org/xnatdev/xnat-web. Expand the menu next to the
'Clone' button and select 'Fork this repository' to create your
own fork.

Clone your fork to your local computer and change into the
`xnat-web` directory

```shell
$ git clone https://bitbucket.org/my-username/xnat-web
$ cd xnat-web
```

## Create a branch to work on ##

When developing XNAT, you should work on a feature branch that
branches off the latest `develop` branch.

```shell
$ git checkout develop
```

### Keep your fork up to date with `xnat-web` ###

Before working on a new feature, bugfix, or other contribution, you
must ensure your fork is up to date.

You will need to define the upstream repository for your fork:

```shell
$ git remote add upstream git@bitbucket.org:xnatdev/xnat-web.git
```

You only need to run the above command once. Then, whenever you would
like to update your fork, you can fetch the upstream, merge into
your fork and push to your repository.

```shell
$ git fetch upstream
$ git checkout develop
$ git merge upstream/develop
$ git push
```

You are then ready to create a new feature branch to work on.

### Submitting a pull request ###

Once you have committed and pushed your changes to your fork,
you should create a pull request to have your changes merged into
`xnat-web`.

Please note, in the description for the pull request
you should link to the issue your pull request addresses.

# Setting up a development environment for XNAT #

Below we will walk through installing the necessary dependencies.

## Installing the Java 8 JDK ##

All XNAT components are currently built with the Java 8 Java Development Kit (JDK).
There are several methods for downloading the JDK.

One option is to use a package manager to download and install the JDK. For example,
if you are using a Mac with an Intel chip, you can use [Brew](https://brew.sh/):

```shell
brew install openjdk@8
```

If you are on Windows, you could use [Chocolately](https://community.chocolatey.org/):

```shell
choco install openjdk8 --version=8.322.06
```

Alterntively, if you have installed IntelliJ, you can install the JDK through your IDE:

- open any project and open the project settings - on a Mac you can use the `⌘+` shortcut
- open `Platform Settings > SDKs` on the left pane
- click the `+` button in the middle pane and select `Download JDK…`
- select version 1.8 to download
- select a vendor, making sure to select one that matches your architecture. For example
  on an M1 Mac you will need an `aarch64` version of the JDK

After installing the JDK, you will need to add it to your path by setting the `JAVA_HOME`
environment variable. You can do this by adding the following command into your shell's
configuration file:

```shell
export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
```

The above assumes you are using bash, zsh, or a similar shell.


## Installing Maven ##

You can use your package manager to install Maven. For example, on Mac you can use Brew:

```shell
$ brew install maven
```

or on Windows you can use Chocolately:


```shell
choco install maven
```

## Installing Gradle ##

Although Gradle is used to build XNAT, you will not need to install it
directly. To build XNAT, you will use a wrapper script
[`gradlew`](./gradlew) that will install Gradle for you.

# Building XNAT #

First check that you have all dependencies installed and confgured correctly.
You can run the following command from the `xnat-web` directory:

```shell
$ ./gradlew --version
```

Once you have all requirements installed, please follow the
[instructions in the main README](./README.md#building) for building XNAT.

# Troubleshooting #

## Out of memory ##

If you run into the following error when building XNAT:

```shell
Gradle build daemon has been stopped: JVM garbage collector thrashing and after running out of JVM memory
```

you can increase the RAM available to Java in your `gradle.properties` file.
You may need around 4 GB or RAM to build XNAT:

```shell
org.gradle.jvmargs=-Xmx4096m
```

## Further help ##

If you run into other issue building XNAT, you can go to the XNAT Discussion Forum
for further help. Please first check to see if anyone else has already asked
about the same issue, and if not feel free to create a new thread.
