# NRG Automation Framework

The NRG automation framework provides a means of storing, managing, and executing scripts within a programmatic
framework. Scripts can be associated with a site-wide level or scoped to a particular entity, identified by a
scope setting and loosely structured entity ID. Script runners can be defined and added dynamically to allow
flexible support for different scripting engines.

## Built-in support

There is default support for the Groovy language, version 2.3.6.

## Accessing the script service

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

## Storing a script

You can store a script at the site-wide and project levels like this:

    :::java
        _service.setScript("foo", "println 'Hello world!', Scope.Site, null);
        _service.setScript("foo", "println 'Hello world!', Scope.Project, 1);

## Running a script

Running a script from the service is straightforward:

    :::java
        final Script script = _service.getScript()
        final Object output = _service.runSiteScript("foo", "one");
        final Object output = _service.runScopedScript("foo", Scope.Project, "1", "one");

The type of the output object depend on what the script returns.

