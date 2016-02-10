# XNAT Web Application #

This is the XNAT Web application build.

### Building ###

You can build with a simple Gradle command:

```bash
gradle clean war
```

You may need to build the [XNAT Gradle plugin](https://bitbucket.org/xnatdev/gradle-xnat-plugin) first, although it should be available on the XNAT Maven repository.

### Deploying ###

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


