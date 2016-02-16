# XNAT Web Application #

This is the XNAT Web application build.

# Building #

## Configuring ##

### Gradle ###

In order for the build to work at the moment (and to be able to import the Gradle project into an IDE), you need to set up some properties in a file named **gradle.properties**. This can be placed in your global properties file, which is located in the folder **.gradle** under your home folder, or in the same folder as the **build.gradle** file.

This properties file must contain values for the following properties:

```
repoUsername=xxx
repoPassword=xxx
deployHost=xxx
deployPort=xxx
deployContext=xxx
deployUser=xxx
deployPassword=xxx
```

The repo properties are used when deploying build artifacts to the Maven repository. The deploy properties are used when deploying to a remote Tomcat server. Note that the values for these properties don't need to be valid! If you're not going to deploy to the Maven repository and you're not going to deploy to a remote Tomcat server, you can use the values shown up above (i.e. "xxx" for everything) and be totally fine. Gradle will pitch a fit if there's not a value for these properties though, so you need to have something in there. We'll try to fix this so that you don't have to have junk values just to make it feel better about itself, but until that time just keep the placekeeper values in there.

There are a lot of other useful properties you can set in **gradle.properties**, so it's worth spending a little time [reading about the various properties Gradle recognizes in this file](https://docs.gradle.org/current/userguide/build_environment.html).

### XNAT ###

You also need to do a little configuring of the XNAT build. This is a temporary requirement until we've deprecated the **InstanceSettings.xml** configuration, but is required until we complete that task.

Before building the XNAT web application, whether via Gradle or the IDE, modify **InstanceSettings.xml** to set the database properties, site URL, archive, cache, and prearchive paths, etc. If you change anything, e.g. move to a different database for some reason, you'll need to modify **InstanceSettings.xml** again and rebuild.

## Building ##

You can build with a simple Gradle command:

```bash
gradle clean war
```

You may need to build the [XNAT Gradle plugin](https://bitbucket.org/xnatdev/gradle-xnat-plugin) first, although it should be available on the XNAT Maven repository.

This will create your deployable web application in the location:

```bash
build/libs/xnat-web-1.7.0-SNAPSHOT.war
```

You can perform a build to your local Maven repository for development purposes like this:

```bash
gradle clean jar publishToMavenLocal
```

You can perform a build deploying to the XNAT Maven repository like this:

```bash
gradle clean jar publishToMavenLocal publishMavenJavaPublicationToMavenRepository
```

For this last one, the values set for **repoUsername** and **repoPassword** must be valid credentials for pushing artifacts to the Maven server.

# Configuring #

You must perform a couple of configuration steps in your run-time environment (e.g. your local development workstation, a Vagrant VM, etc.) in order for XNAT to run properly:

* In your Tomcat start-up configuration, add **-Dxnat.home=<path>** where **<path>** is some writeable location. This is where XNAT will look for its configuration and logs folders, e.g. **${xnat.home}/config** and **${xnat.home}/logs**.
* Copy **services.properties** into the **config** folder underneath the path you specified for **xnat.home**. For example, I set **xnat.home** to **~/xnat**. Under that I have the folder **config**, which contains **services.properties** (you don't have to create **logs**: log4j will create it if it doesn't exist).
* Open **InstanceSettings.xml** and modify the settings in there to reflect your configuration. You must do this before the build. This is an annoying but temporary requirement.

# Running XNAT #

## From Gradle ##

You can deploy the built war to a remote Tomcat using the Cargo plugin.

```bash
gradle cargoDeployRemote
gradle cargoRedeployRemote
gradle cargoUndeployRemote
```

As you can probably guess, the first task deploys the application to the remote Tomcat. If there is already an application deployed at the specified context, this task will fail. In that case you can use the second task to redeploy. The third task undeploys the remote application, clearing the context.

You'll need to have installed the [Tomcat manager application](https://tomcat.apache.org/tomcat-7.0-doc/manager-howto.html) onto the Tomcat server. You'll also need to configure the **tomcat-users.xml** file appropriately. This is described in the Tomcat Manager How-to page, but a sample **tomcat-users.xml** might look like this:

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

* **deployHost** is the address for the VM hosting the remote Tomcat.
* **deployPort** is the port the Tomcat is running on. Note that this should be the Tomcat port, not the proxy server, e.g. nginx or httpd.
* **deployContext** is the context, i.e. application path, to which you want to deploy XNAT. This can be / or /xnat or whatever.
* **deployUser** is the username configured in the Tomcat manager.
* **deployPassword** is the password for the user.

The easiest way to specify these values is to put them in your **gradle.properties** file, usually located in ~/.gradle but you can also have it in the root of the build folder.

```
deployHost=xnat17dev.xnat.org
deployPort=8080
deployContext=/
deployUser=deploy
deployPassword=s3cret
```

You can also specify these on the Gradle command line. Just take each setting and preface it with **-D**.

## In the IDE ##

You can run XNAT from within your IDE. You need to set the appropriate values for the VM to configure available heap and permgen space, as well as setting **xnat.home** appropriately. For example, in IntelliJ, I have the following line for the VM options in my local Tomcat debug configuration:

```-
Xms512m -Xmx1g -XX:MaxPermSize=256m -Dxnat.home=/Users/xxx/xnat
```