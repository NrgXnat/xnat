package org.nrg.automation.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.automation.entities.Script;
import org.nrg.automation.entities.ScriptOutput;
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

    public static final String GROOVY_HELLO_PAYLOAD = "hi there";
    public static final String GROOVY_HELLO_WORLD = "println \""+ GROOVY_HELLO_PAYLOAD + "\"\n" +
            "\""+ GROOVY_HELLO_PAYLOAD + "\"";
    public static final String GROOVY_HELLO_PROJECT = "def message = \"hi there ${scope} ${entityId}\"\n" +
            "println message\n" +
            "message.toString()";
    public static final String GROOVY_IMPORT = "import org.apache.commons.lang3.StringUtils\n" +
            "def isBlank = StringUtils.isBlank(\"hi there\")\n" +
            "def sentence = \"'hi there' \" + (isBlank ? \"is\" : \"is not\") + \" blank\"\nsentence";
    public static final String GROOVY_VARIABLE = "println \"I found this variable: ${variable}\"\n" +
            "variable";
    public static final String GROOVY_OBJECT = "def map = [\"one\" : 1, \"two\" : 2, \"three\" : 3]\n" +
            "map << [\"four\" : submit]\n" +
            "map.each { key, value ->\n" +
            "    println \"${key}: ${value}\"\n" +
            "}\n" +
            "map\n";
    public static final String JS_HELLO_PAYLOAD = "Hello there from Javascript";
    public static final String JS_HELLO_WORLD = "print('" + JS_HELLO_PAYLOAD + "');\n'" + JS_HELLO_PAYLOAD + "';";
    public static final String PYTHON_HELLO_PAYLOAD = "Hello there from Python";
    public static final String PYTHON_HELLO_WORLD = "print '" + PYTHON_HELLO_PAYLOAD + "'";
    public static final String ID_PROJECT_1 = "1";
    public static final String SCRIPT_ID_1 = "one";
    public static final String SCRIPT_ID_2 = "two";
    public static final String SCRIPT_ID_3 = "three";

    @Test
    public void testRunnerSerialization() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        final List<String> runners = _service.getRunners();
        final String json = mapper.writeValueAsString(runners);
        assertNotNull(json);
    }

    @Test
    public void addRetrieveAndRunSiteScriptTests() throws NrgServiceException {
        _service.setScript(SCRIPT_ID_1, GROOVY_HELLO_WORLD, Scope.Site, null, "EVENT1");
        _service.setScript(SCRIPT_ID_2, JS_HELLO_WORLD, Scope.Site, null, "EVENT2", "JavaScript");
        _service.setScript(SCRIPT_ID_3, PYTHON_HELLO_WORLD, Scope.Site, null, "EVENT3", "Python");

        final Script script1 = _service.getScript(SCRIPT_ID_1);
        assertNotNull(script1);
        assertEquals(GROOVY_HELLO_WORLD, script1.getContent());
        assertEquals("groovy", script1.getLanguage());

        final Script script2 = _service.getScript(SCRIPT_ID_2);
        assertNotNull(script2);
        assertEquals(JS_HELLO_WORLD, script2.getContent());
        assertEquals("JavaScript", script2.getLanguage());

        final Script script3 = _service.getScript(SCRIPT_ID_3);
        assertNotNull(script3);
        assertEquals(PYTHON_HELLO_WORLD, script3.getContent());
        assertEquals("Python", script3.getLanguage());

        final ScriptOutput output1 = _service.runScript(script1);
        assertNotNull(output1);
        assertNotNull(output1.getResults());
        assertTrue(output1.getResults() instanceof String);
        assertEquals(GROOVY_HELLO_PAYLOAD, output1.toString());

        final ScriptOutput output2 = _service.runScript(script2);
        assertNotNull(output2);
        assertNotNull(output2.getResults());
        assertTrue(output2.getResults() instanceof String);
        assertEquals(JS_HELLO_PAYLOAD, output2.toString());

        // Python doesn't output a result the same way other scripting languages do. We have to look at the console instead.
        final ScriptOutput output3 = _service.runScript(script3);
        assertNotNull(output3);
        assertNull(output3.getResults());
        assertEquals(PYTHON_HELLO_PAYLOAD, output3.getOutput().trim());
    }

    @Test
    public void addRetrieveAndRunProjectScriptTest() throws NrgServiceException {
        _service.setScript(SCRIPT_ID_1, GROOVY_HELLO_PROJECT, Scope.Project, ID_PROJECT_1);
        final List<Script> scripts = _service.getScripts(Scope.Project, ID_PROJECT_1);
        assertNotNull(scripts);
        assertEquals(1, scripts.size());

        final Script script = scripts.get(0);
        assertEquals(SCRIPT_ID_1, script.getScriptId());
        assertEquals(GROOVY_HELLO_PROJECT, script.getContent());

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("scope", Scope.Project.code());
        parameters.put("entityId", ID_PROJECT_1);
        parameters.put("event", ScriptTrigger.DEFAULT_EVENT);
        //parameters.put("scriptVersion", "1");
        final ScriptOutput output = _service.runScript(script, parameters);
        assertNotNull(output);
        assertNotNull(output.getResults());
        assertTrue(output.getResults() instanceof String);
        assertEquals("hi there " + Scope.Project.code() + " " + ID_PROJECT_1, output.getResults());
    }

    @Test
    public void useImportedClassTest() throws NrgServiceException {
        _service.setScript(SCRIPT_ID_1, GROOVY_IMPORT);
        final Script script = _service.getScript(SCRIPT_ID_1);
        assertNotNull(script);
        assertEquals(SCRIPT_ID_1, script.getScriptId());
        final ScriptOutput output = _service.runScript(script);
        assertNotNull(output);
        assertNotNull(output.getResults());
        assertTrue(output.getResults() instanceof String);
        assertEquals("'hi there' is not blank", output.getResults());
    }

    @Test
    public void returnComplexObject() throws NrgServiceException {
        _service.setScript(SCRIPT_ID_1, GROOVY_OBJECT);

        final Script script = _service.getScript(SCRIPT_ID_1);
        assertNotNull(script);
        assertEquals(SCRIPT_ID_1, script.getScriptId());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("submit", 4);
        //parameters.put("scriptVersion", "1");
        final ScriptOutput output = _service.runScript(script, parameters);
        assertNotNull(output);
        assertNotNull(output.getResults());
        assertTrue(output.getResults() instanceof Map);
        assertTrue(1 == (Integer) ((Map) output.getResults()).get("one"));
        assertTrue(2 == (Integer) ((Map) output.getResults()).get("two"));
        assertTrue(3 == (Integer) ((Map) output.getResults()).get("three"));
        assertTrue(4 == (Integer) ((Map) output.getResults()).get("four"));
    }

    @Test
    public void passVariablesTest() throws NrgServiceException {
        _service.setScript(SCRIPT_ID_1, GROOVY_VARIABLE);

        final Script script = _service.getScript(SCRIPT_ID_1);
        assertNotNull(script);
        assertEquals(SCRIPT_ID_1, script.getScriptId());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("variable", "This is a value!");
        //parameters.put("scriptVersion", "1");
        final ScriptOutput output = _service.runScript(script, parameters);
        assertNotNull(output);
        assertNotNull(output.getResults());
        assertTrue(output.getResults() instanceof String);
        assertEquals("This is a value!", output.getResults());
    }

    @Test
    @Ignore("These methods need to be tested, but aren't right now.")
    public void callOtherSetScriptFunctions() {
        final Script script = new Script();
        final String scriptId = "";
        final String content = "";
        final String description = "";
        final Scope scope = Scope.Project;
        final String entityId = "";
        final String event = "";
        _service.setScript(scriptId, content, description);
        _service.setScript(scriptId, content, scope, entityId, event);
        _service.setScript(scriptId, content, description, scope, entityId);
        _service.setScript(scriptId, content, description, scope, entityId, event);
        _service.setScript(script, scope, entityId, event);
    }

    @Inject
    private ScriptRunnerService _service;
}
