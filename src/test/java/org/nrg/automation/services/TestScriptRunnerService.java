package org.nrg.automation.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.constants.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TestScriptRunnerService {

    public static final String SCRIPT_HELLO_WORLD = "println \"hi there\"\n" +
            "\"hi there\"";
    public static final String SCRIPT_HELLO_PROJECT = "def message = \"hi there ${scope} ${entityId}\"\n" +
            "println message\n" +
            "message.toString()";
    public static final String SCRIPT_IMPORT = "import org.apache.commons.lang3.StringUtils\n" +
            "def isBlank = StringUtils.isBlank(\"hi there\")\n" +
            "def sentence = \"'hi there' \" + (isBlank ? \"is\" : \"is not\") + \" blank\"\nsentence";
    public static final String SCRIPT_VARIABLE = "println \"I found this variable: ${variable}\"\n" +
            "variable";
    public static final String SCRIPT_OBJECT = "def map = [\"one\" : 1, \"two\" : 2, \"three\" : 3]\n" +
            "map << [\"four\" : submit]\n" +
            "map.each { key, value ->\n" +
            "    println \"${key}: ${value}\"\n" +
            "}\n" +
            "map\n";
    public static final String ID_PROJECT_1 = "1";
    public static final String ID_FAILOVER1 = "failover1";
    public static final String ID_FAILOVER2 = "failover2";
    public static final String SCRIPT_FAILOVER1_SITE = "\"" + ID_FAILOVER1 + " site\"";
    public static final String SCRIPT_FAILOVER1_PROJECT = "\"" + ID_FAILOVER1 + " project\"";
    public static final String SCRIPT_FAILOVER2_SITE = "\"" + ID_FAILOVER2 + " site\"";
    public static final String USER1 = "one";

    @Test
    public void addRetrieveAndRunSiteScriptTest() throws ConfigServiceException {
        _service.setSiteScript(USER1, "one", SCRIPT_HELLO_WORLD);
        final Properties script = _service.getSiteScript("one");
        assertNotNull(script);
        assertTrue(script.containsKey(ScriptProperty.Script.key()));
        assertEquals(SCRIPT_HELLO_WORLD, script.getProperty(ScriptProperty.Script.key()));

        final boolean hasScript = _service.hasSiteScript("one");
        assertTrue(hasScript);

        final Object output = _service.runSiteScript(USER1, "one");
        assertNotNull(output);
        assertTrue(output instanceof String);
        assertEquals("hi there", output);
    }

    @Test
    public void addRetrieveAndRunProjectScriptTest() throws ConfigServiceException {
        _service.setScopedScript(USER1, Scope.Project, ID_PROJECT_1, "one", SCRIPT_HELLO_PROJECT);
        final Properties script = _service.getScopedScript(Scope.Project, ID_PROJECT_1, "one");
        assertNotNull(script);
        assertTrue(script.containsKey(ScriptProperty.Script.key()));
        assertEquals(SCRIPT_HELLO_PROJECT, script.getProperty(ScriptProperty.Script.key()));

        final boolean hasScript = _service.hasScopedScript(Scope.Project, ID_PROJECT_1, "one");
        assertTrue(hasScript);

        final Object output = _service.runScopedScript(USER1, Scope.Project, ID_PROJECT_1, "one");
        assertNotNull(output);
        assertTrue(output instanceof String);
        assertEquals("hi there " + Scope.Project.code() + " 1", output);
    }

    @Test
    public void addRetrieveAndRunFailoverScriptsTest() throws ConfigServiceException {
        // Set up scripts for failover1 at the project and site level.
        _service.setSiteScript(USER1, ID_FAILOVER1, SCRIPT_FAILOVER1_SITE);
        _service.setScopedScript(USER1, Scope.Project, ID_PROJECT_1, ID_FAILOVER1, SCRIPT_FAILOVER1_PROJECT);

        final Properties siteScript1 = _service.getSiteScript(ID_FAILOVER1);
        assertNotNull(siteScript1);
        assertTrue(siteScript1.containsKey(ScriptProperty.Script.key()));
        assertEquals(SCRIPT_FAILOVER1_SITE, siteScript1.getProperty(ScriptProperty.Script.key()));
        assertTrue(_service.hasSiteScript(ID_FAILOVER1));

        final Properties projectScript = _service.getScopedScript(Scope.Project, ID_PROJECT_1, ID_FAILOVER1);
        assertNotNull(projectScript);
        assertTrue(projectScript.containsKey(ScriptProperty.Script.key()));
        assertEquals(SCRIPT_FAILOVER1_PROJECT, projectScript.getProperty(ScriptProperty.Script.key()));
        assertTrue(_service.hasScopedScript(Scope.Project, ID_PROJECT_1, ID_FAILOVER1));

        // Set up script for failover2 at the site level.
        _service.setSiteScript(USER1, ID_FAILOVER2, SCRIPT_FAILOVER2_SITE);
        final Properties siteScript2 = _service.getSiteScript(ID_FAILOVER2);
        assertNotNull(siteScript2);
        assertTrue(siteScript2.containsKey(ScriptProperty.Script.key()));
        assertEquals(SCRIPT_FAILOVER2_SITE, siteScript2.getProperty(ScriptProperty.Script.key()));
        assertTrue(_service.hasSiteScript(ID_FAILOVER1));

        // Now verify that there are scripts for both IDs.
        assertTrue(_service.hasScript(Scope.Project, ID_PROJECT_1, ID_FAILOVER1));
        assertTrue(_service.hasScript(Scope.Project, ID_PROJECT_1, ID_FAILOVER2));

        final Object outputFailover1 = _service.runScript(USER1, Scope.Project, ID_PROJECT_1, ID_FAILOVER1);
        assertNotNull(outputFailover1);
        assertTrue(outputFailover1 instanceof String);
        assertEquals(ID_FAILOVER1 + " project", outputFailover1);
        final Object outputFailover2 = _service.runScript(USER1, Scope.Project, ID_PROJECT_1, ID_FAILOVER2);
        assertNotNull(outputFailover2);
        assertTrue(outputFailover2 instanceof String);
        assertEquals(ID_FAILOVER2 + " site", outputFailover2);
    }

    @Test
    public void useImportedClassTest() throws ConfigServiceException {
        _service.setSiteScript(USER1, "two", SCRIPT_IMPORT);
        final Object output = _service.runSiteScript(USER1, "two");
        assertNotNull(output);
        assertTrue(output instanceof String);
        assertEquals("'hi there' is not blank", output);
    }

    @Test
    public void passVariablesTest() throws ConfigServiceException {
        _service.setSiteScript(USER1, "three", SCRIPT_VARIABLE);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("variable", "This is a value!");
        final Object output = _service.runSiteScript(USER1, "three", parameters);
        assertNotNull(output);
        assertTrue(output instanceof String);
        assertEquals("This is a value!", output);
    }

    @Test
    public void returnComplexObject() throws ConfigServiceException {
        _service.setSiteScript(USER1, "four", SCRIPT_OBJECT);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("submit", 4);
        final Object output = _service.runSiteScript(USER1, "four", parameters);
        assertNotNull(output);
        assertTrue(output instanceof Map);
        assertTrue(1 == (Integer) ((Map) output).get("one"));
        assertTrue(2 == (Integer) ((Map) output).get("two"));
        assertTrue(3 == (Integer) ((Map) output).get("three"));
        assertTrue(4 == (Integer) ((Map) output).get("four"));
    }

    @Inject
    private ScriptRunnerService _service;
}
