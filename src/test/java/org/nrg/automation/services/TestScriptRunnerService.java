package org.nrg.automation.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.automation.entities.Script;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@TransactionConfiguration(defaultRollback = true)
@Transactional
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
    public static final String SCRIPT_ID_1 = "one";

    @Test
    public void addRetrieveAndRunSiteScriptTest() throws NrgServiceException {
        _service.setScript(SCRIPT_ID_1, SCRIPT_HELLO_WORLD, Scope.Site, null);
        final Script script = _service.getScript(SCRIPT_ID_1);
        assertNotNull(script);
        assertEquals(SCRIPT_HELLO_WORLD, script.getContent());

        final Script retrieved = _service.getScript(SCRIPT_ID_1);
        assertNotNull(retrieved);

        final Object output = _service.runScript(retrieved);
        assertNotNull(output);
        assertTrue(output instanceof String);
        assertEquals("hi there", output);
    }

    @Test
    public void addRetrieveAndRunProjectScriptTest() throws NrgServiceException {
        _service.setScript(SCRIPT_ID_1, SCRIPT_HELLO_PROJECT, Scope.Project, ID_PROJECT_1);
        final List<Script> scripts = _service.getScripts(Scope.Project, ID_PROJECT_1);
        assertNotNull(scripts);
        assertEquals(1, scripts.size());

        final Script script = scripts.get(0);
        assertEquals(SCRIPT_ID_1, script.getScriptId());
        assertEquals(SCRIPT_HELLO_PROJECT, script.getContent());

        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("scope", Scope.Project.code());
        parameters.put("entityId", ID_PROJECT_1);
        parameters.put("event", ScriptTrigger.DEFAULT_EVENT);
        final Object output = _service.runScript(script, parameters);
        assertNotNull(output);
        assertTrue(output instanceof String);
        assertEquals("hi there " + Scope.Project.code() + " " + ID_PROJECT_1, output);
    }

    @Test
    public void useImportedClassTest() throws NrgServiceException {
        _service.setScript(SCRIPT_ID_1, SCRIPT_IMPORT);
        final Script script = _service.getScript(SCRIPT_ID_1);
        assertNotNull(script);
        assertEquals(SCRIPT_ID_1, script.getScriptId());
        final Object output = _service.runScript(script);
        assertNotNull(output);
        assertTrue(output instanceof String);
        assertEquals("'hi there' is not blank", output);
    }

    @Test
    public void returnComplexObject() throws NrgServiceException {
        _service.setScript(SCRIPT_ID_1, SCRIPT_OBJECT);

        final Script script = _service.getScript(SCRIPT_ID_1);
        assertNotNull(script);
        assertEquals(SCRIPT_ID_1, script.getScriptId());

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("submit", 4);

        final Object output = _service.runScript(script, parameters);
        assertNotNull(output);
        assertTrue(output instanceof Map);
        assertTrue(1 == (Integer) ((Map) output).get("one"));
        assertTrue(2 == (Integer) ((Map) output).get("two"));
        assertTrue(3 == (Integer) ((Map) output).get("three"));
        assertTrue(4 == (Integer) ((Map) output).get("four"));
    }

    @Test
    public void passVariablesTest() throws NrgServiceException {
        _service.setScript(SCRIPT_ID_1, SCRIPT_VARIABLE);

        final Script script = _service.getScript(SCRIPT_ID_1);
        assertNotNull(script);
        assertEquals(SCRIPT_ID_1, script.getScriptId());

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("variable", "This is a value!");
        final Object output = _service.runScript(script, parameters);
        assertNotNull(output);
        assertTrue(output instanceof String);
        assertEquals("This is a value!", output);
    }

    @Inject
    private ScriptRunnerService _service;
}
