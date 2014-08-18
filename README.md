# NRG Automation Framework

The NRG automation framework provides a means of storing, managing, and executing scripts within a programmatic
framework. Scripts can be associated with a site-wide level or scoped to a particular entity, identified by a
scope setting and loosely structured entity ID. Script runners can be defined and added dynamically to allow
flexible support for different scripting engines.

## Built-in support

There is default support for the Groovy language, version 2.3.6.

## Accessing the script service

The default script runner service is available as a Spring service. You must also have access to the
(NRG configuration service)[https://bitbucket.org/nrg/nrg\_config):

    :::xml
        <context:component-scan base-package="org.nrg.config.daos"/>
        <context:component-scan base-package="org.nrg.automation.services.impl"/>
        <context:component-scan base-package="org.nrg.automation.runners"/>

        <bean class="org.nrg.config.services.impl.DefaultConfigService"/>

        <bean id="nrgConfigPackages" class="org.nrg.framework.orm.hibernate.HibernateEntityPackageList">
            <property name="items">
                <list>
                    <value>org.nrg.config.entities</value>
                </list>
            </property>
        </bean>

Then in your Java code, use **@Inject** or **@Autowired** to associate the instance of the runner service with
a member variable:

    :::java
        @Inject
        private ScriptRunnerService _service;

## Storing a script

You can store a script at the site-wide and project levels like this:

    :::java
        _service.setSiteScript("foo", "one", SCRIPT_HELLO_WORLD);
        _service.setScopedScript("foo", Scope.Project, 1, "one", SCRIPT_HELLO_PROJECT);

Note that, when a script is scoped to a particular entity, the information about the scope is passed into the
script in the variables scope and entityId.

## Running a script

Running a script from the service is straightforward:

    :::java
        final Object output = _service.runSiteScript("foo", "one");
        final Object output = _service.runScopedScript("foo", Scope.Project, "1", "one");

The type of the output object depend on what the script returns.

