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

    @Test
    public void addRetrieveAndRunSiteScriptTest() throws ConfigServiceException {
        _service.setSiteScript("foo", "one", SCRIPT_HELLO_WORLD);
        final Properties script = _service.getSiteScript("one");
        assertNotNull(script);
        assertTrue(script.containsKey(ScriptProperty.Script.key()));
        assertEquals(SCRIPT_HELLO_WORLD, script.getProperty(ScriptProperty.Script.key()));
        final Object output = _service.runSiteScript("foo", "one");
        assertNotNull(output);
        assertTrue(output instanceof String);
        assertEquals("hi there", output);
    }

    @Test
    public void addRetrieveAndRunProjectScriptTest() throws ConfigServiceException {
        _service.setScopedScript("foo", Scope.Project, "1", "one", SCRIPT_HELLO_PROJECT);
        final Properties script = _service.getScopedScript(Scope.Project, "1", "one");
        assertNotNull(script);
        assertTrue(script.containsKey(ScriptProperty.Script.key()));
        assertEquals(SCRIPT_HELLO_PROJECT, script.getProperty(ScriptProperty.Script.key()));
        final Object output = _service.runScopedScript("foo", Scope.Project, "1", "one");
        assertNotNull(output);
        assertTrue(output instanceof String);
        assertEquals("hi there " + Scope.Project.code() + " 1", output);
    }

    @Test
    public void useImportedClassTest() throws ConfigServiceException {
        _service.setSiteScript("foo", "two", SCRIPT_IMPORT);
        final Object output = _service.runSiteScript("foo", "two");
        assertNotNull(output);
        assertTrue(output instanceof String);
        assertEquals("'hi there' is not blank", output);
    }

    @Test
    public void passVariablesTest() throws ConfigServiceException {
        _service.setSiteScript("foo", "three", SCRIPT_VARIABLE);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("variable", "This is a value!");
        final Object output = _service.runSiteScript("foo", "three", parameters);
        assertNotNull(output);
        assertTrue(output instanceof String);
        assertEquals("This is a value!", output);
    }

    @Test
    public void returnComplexObject() throws ConfigServiceException {
        _service.setSiteScript("foo", "four", SCRIPT_OBJECT);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("submit", 4);
        final Object output = _service.runSiteScript("foo", "four", parameters);
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
