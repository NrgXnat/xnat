NRG Automation Framework
================================

The NRG automation framework provides a means of storing, managing, and executing scripts within a programmatic
framework. Scripts can be associated with a site-wide level or scoped to a particular entity, identified by a
scope setting and loosely structured entity ID. Script runners can be defined and added dynamically to allow
flexible support for different scripting engines.

Building
--------

To build the NRG automation framework, invoke Maven with the desired lifecycle phase.
For example, the following command cleans previous builds, builds a new jar file, 
creates archives containing the source code and JavaDocs for the library, runs the 
library's unit tests, and installs the jar into the local repository:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
mvn clean install
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Built-in support
--------

There is default support for the Groovy language. The Groovy version depends on the version of the
Groovy dependency specified either in this library's POM or in the parent POM.

Accessing the script service
--------

The default script runner service is available as a Spring service. You can include the default
script runner service by referencing the built-in context configuration:

    :::xml
        <import resource="classpath:/META-INF/configuration/nrg-automation-context.xml" />

Then in your Java code, use **@Inject** or **@Autowired** to associate the instance of the runner service with
a member variable:

    :::java
        @Inject
        private ScriptRunnerService _service;

You can access the **ScriptService** and **ScriptTriggerService** instances in the same way:

    :::java
        @Inject
        private ScriptService _scriptService;
        @Inject
        private ScriptTriggerService _triggerService;

Storing a script
--------

You can store a script at the site-wide and project levels like this:

    :::java
        _service.setScript("foo", "println 'Hello world!', Scope.Site, null);
        _service.setScript("foo", "println 'Hello world!', Scope.Project, 1);

Running a script
--------

Running a script from the service is straightforward:

    :::java
        final Script script = _service.getScript()
        final Object output = _service.runSiteScript("foo", "one");
        final Object output = _service.runScopedScript("foo", Scope.Project, "1", "one");

The type of the output object depend on what the script returns.

