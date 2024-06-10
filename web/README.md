# XNAT Web Application #

This is the XNAT Web application source code repository.

# Installing #

If you just want to install the latest release version of XNAT, find it on the [XNAT download page](https://download.xnat.org).

You will also need to download the [XNAT pipeline engine](https://github.com/nrgxnat/xnat-pipeline-engine), which is also available on the [XNAT download 
page](https://download.xnat.org).

If you would like to build a virtual machine that can run XNAT, try using the [XNAT Vagrant project](https://bitbucket.org/xnatdev/xnat-vagrant).

If you would like to contribute to `xnat-web`, please see the
[contributing guide](./contributing.md).

# Building #

## Configuring ##

### Gradle ###

In order for the build to work at the moment (and to be able to import the Gradle project into an IDE), you need to set up some properties in a file named **gradle.properties**.
This can be placed in your global properties file, which is located in the folder **.gradle** under your home folder, or in the same folder as the **build.gradle** file.

This properties file must contain values for the following properties:

```properties
repoUsername=xxx
repoPassword=xxx
deployHost=xxx
deployPort=xxx
deployContext=xxx
deployUser=xxx
deployPassword=xxx
```

The repo properties are used when deploying build artifacts to the Maven repository. The deploy properties are used when deploying to a remote Tomcat server. Note that the 
values for these properties don't need to be valid! If you're not going to deploy to the Maven repository and you're not going to deploy to a remote Tomcat server, you can 
use the values shown up above (i.e. "xxx" for everything) and be totally fine. Gradle will pitch a fit if there's not a value for these properties though, so you need to have 
something in there. We'll try to fix this so that you don't have to have junk values just to make it feel better about itself, but until that time just keep the placeholder
values in there.

There are a lot of other useful properties you can set in **gradle.properties**, so it's worth spending a little time [reading about the various properties Gradle recognizes
in this file](https://docs.gradle.org/current/userguide/build_environment.html).

### XNAT Configuration ###

You also need to add another initial configuration file in your home directory. Create a directory path **xnat/config** in your home directory and a file named **xnat-conf.properties**
there. In this file, define the following properties:

```properties
datasource.driver=org.postgresql.Driver
datasource.url=jdbc:postgresql://localhost/<XNAT Instance Name>
datasource.username=<database username>
datasource.password=<database password>

hibernate.dialect=org.hibernate.dialect.PostgreSQL9Dialect
hibernate.hbm2ddl.auto=update
hibernate.show_sql=false
hibernate.cache.use_second_level_cache=true
hibernate.cache.use_query_cache=true
```

IMPORTANT NOTE: You'll definitely want to fill in the three \<placeholders>–\<XNAT Instance Name>, \<database username> and \<database password>–above!

## Building ##

First clone the source repository for XNAT Web:

```shell
$ git clone https://bitbucket.org/xnatdev/xnat-web
Cloning into 'xnat-web'...
remote: Counting objects: 41875, done.
remote: Compressing objects: 100% (21733/21733), done.
remote: Total 41875 (delta 25224), reused 26349 (delta 16049)
Receiving objects: 100% (41875/41875), 52.62 MiB | 5.23 MiB/s, done.
Resolving deltas: 100% (25224/25224), done.
Checking out files: 100% (5096/5096), done.
$ cd xnat-web
```

You can build using the **gradlew** (or **gradlew.bat** on Windows) wrapper script in the **xnat-web** repo:

```shell
$ ./gradlew clean war
```

You may need to build the [XDAT Data Builder Gradle plugin](https://bitbucket.org/xnatdev/xdat-data-builder) and [XNAT Data Models library](https://bitbucket.org/xnatdev/xnat-data-models) first,
although these should be available on the [XNAT Maven repository](https://nrgxnat.jfrog.io/nrgxnat).

This should create a deployable web application in the location:

```shell
build/libs/xnat-web-1.7.6.war
```

Build and publish to your local Maven repository (usually located at **~/.m2/repository**) for development purposes like this:

```shell
$ ./gradlew clean jar publishToMavenLocal
```

Build and deploy to the XNAT Maven repository like this:

```shell
$ ./gradlew clean jar publishToMavenLocal publish
```

For this last one, the values set for **repoUsername** and **repoPassword** must be valid credentials for pushing artifacts to the Maven server. The *publish* task actually comprises
a number of other tasks, which will publish all available artifacts to each repository defined in the *repositories* configuration in the *build.gradle* file. Practically speaking this 
means that *publish* is an alias for the *publishMavenJavaPublicationToMavenRepository* task.

You can specify the name of the generated WAR file (and thus the application context of the application within the Tomcat server) from the command line or a properties file.

On the command line, add the flag **-ParchiveName=name[.war]** to your Gradle command (.war will be appended if it’s not specified). This may look something like this:

```shell
$ ./gradlew -ParchiveName=ROOT.war war
```

You can also set the **archiveName** value in the **gradle.properties** file. **gradle.properties** can be in your repository folder, thus affecting only the local build, or in **~/.gradle/gradle.properties**, which will affect any build that uses the **archiveName** property. To set this value in **gradle.properties**, just add the line:

```properties
archiveName=ROOT
```

If you don’t explicitly set **archiveName**, the build uses the naming scheme **xnat-web-_version_.war**.

Note that **gradle.properties** is in this repository's **.gitignore** file, so if you create a local version you won’t get the annoying “Untracked files” message from git.

# Configuring #

You must perform a couple of configuration steps in your run-time environment (e.g. your local development workstation, a Vagrant VM, etc.) in order for XNAT to run properly:

* In your Tomcat start-up configuration, add **-Dxnat.home=<path>** where **<path>** is some writeable location. This is where XNAT looks for its configuration and logs folders, 
 e.g. **${xnat.home}/config** and **${xnat.home}/logs**.
* Copy **xnat-conf.properties** into the **config** folder underneath the path you specified for **xnat.home**. For example, if you set **xnat.home** to **~/xnat**, under that 
 would be the folder **config**, which contains **xnat-conf.properties** (you don't have to create **logs**: log4j will create it if it doesn't already exist).

# Running XNAT #

## From Gradle ##

Deploy your generated war file to a local Tomcat instance with the **deployToTomcat** task. Unlike the Cargo tasks described below, this task doesn't go through the Tomcat manager
 or transfer the war via network connection. Instead, it copies the physical war file to a local folder named **webapps**. The key is that you need to specify the location of your
 Tomcat instance with the property **tomcatHome**. As with **archiveName** above, you can specify this on the command line:

```shell
$ ./gradlew -PtomcatHome=/var/lib/tomcat
```

Or in the **gradle.properties** file:

```properties
tomcatHome=/var/lib/tomcat
```

**tomcatHome** defaults to ‘.’, so if you don’t specify a value for it, you’ll end up with a folder named **webapps** in your local repository folder. That folder is also in
**.gitignore** so you won’t get bugged by git. You probably didn't intend to have another copy of the war file somewhere in your development folder, but it’s better than copying
files off to random locations or pitching a fit.

Note that **deployToTomcat** depends on the **war** task, so there’s no need to specify the **war** task explicitly.

This provides an efficient workflow for development on a VM or server: set the two values in **gradle.properties** and you can quickly redeploy:

```shell
$ ./gradlew deployToTomcat
```

Gradle compiles or packages any changes since the last build as necessary, re-packages your application into a war if needed, and copies the resulting war file into the Tomcat 
**webapps** folder. It’s worth noting if nothing has changed the war won’t be regenerated or copied.

You can deploy the built war to a remote Tomcat using the Cargo plugin.

```shell
$ ./gradlew cargoDeployRemote
$ ./gradlew cargoRedeployRemote
$ ./gradlew cargoUndeployRemote
```

As you can probably guess, the first task deploys the application to the remote Tomcat. If there is already an application deployed at the specified context, this task will fail.
In that case you can use the second task to redeploy. The third task undeploys the remote application, clearing the context.

You'll need to have installed the [Tomcat manager application](https://tomcat.apache.org/tomcat-8.5-doc/manager-howto.html) onto the Tomcat server. You'll also need to configure
the **tomcat-users.xml** file appropriately. This is described in the Tomcat Manager How-to page, but a sample **tomcat-users.xml** might look like this:

```xml
<?xml version='1.0' encoding='utf-8'?>
<tomcat-users>
    <role rolename="manager-gui"/>
    <role rolename="manager-script"/>
    <role rolename="manager-jmx"/>
    <user username="admin" password="s3cret" roles="manager-gui"/>
    <user username="deploy" password="s3cret" roles="manager-script,manager-jmx"/>
</tomcat-users>
```

You need to pass in a few values when you run the Gradle build with any of the Cargo remote tasks:

| Task | Description |
| :--- | :---------- |
| **deployHost** | The address for the VM hosting the remote Tomcat. |
| **deployPort** | The port the Tomcat is running on. Note that this should be the Tomcat port, not the proxy server, e.g. nginx or httpd. |
| **deployContext** | The context, i.e. application path, to which you want to deploy XNAT. This can be / or /xnat or whatever. |
| **deployUser** | The username configured in the Tomcat manager. |
| **deployPassword** | The password for the user. |

The easiest way to specify these values is to put them in your **gradle.properties** file, usually located in ~/.gradle but you can also have it in the root of the build folder.

```properties
deployHost=xnatdev.xnat.org
deployPort=8080
deployContext=/
deployUser=deploy
deployPassword=s3cret
```

You can also specify these on the Gradle command line. Just take each setting and preface it with **-D**.

## In the IDE ##

You can run XNAT from within your IDE. You need to set the appropriate values for the VM to configure available heap and permgen space, as well as setting **xnat.home** appropriately.
For example, in IntelliJ, I have the following line for the VM options in my local Tomcat debug configuration:

```shell
Xms512m -Xmx1g -XX:MaxPermSize=256m -Dxnat.home=/Users/xxx/xnat
```
