/*
 * automation: org.nrg.automation.services.TestScriptRunnerService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.nrg.automation.configuration.AutomationTestsConfiguration;
import org.nrg.automation.entities.Script;
import org.nrg.automation.entities.ScriptOutput;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.services.SerializerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AutomationTestsConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Slf4j
public class TestScriptRunnerService {
    public static final String                    GROOVY_HELLO_PAYLOAD = "hi there";
    public static final String                    GROOVY_HELLO_WORLD   = "println \"" + GROOVY_HELLO_PAYLOAD + "\"\n" +
                                                                         "\"" + GROOVY_HELLO_PAYLOAD + "\"";
    public static final String                    GROOVY_HELLO_PROJECT = "def message = \"hi there ${scope} ${entityId}\"\n" +
                                                                         "println message\n" +
                                                                         "message.toString()";
    public static final String                    GROOVY_IMPORT        = "import org.apache.commons.lang3.StringUtils\n" +
                                                                         "def isBlank = StringUtils.isBlank(\"hi there\")\n" +
                                                                         "def sentence = \"'hi there' \" + (isBlank ? \"is\" : \"is not\") + \" blank\"\nsentence";
    public static final String                    GROOVY_VARIABLE      = "println \"I found this variable: ${variable}\"\n" +
                                                                         "variable";
    public static final String                    GROOVY_OBJECT        = "def map = [\"one\" : 1, \"two\" : 2, \"three\" : 3]\n" +
                                                                         "map << [\"four\" : submit]\n" +
                                                                         "map.each { key, value ->\n" +
                                                                         "    println \"${key}: ${value}\"\n" +
                                                                         "}\n" +
                                                                         "map\n";
    public static final String                    JS_HELLO_PAYLOAD     = "Hello there from Javascript";
    public static final String                    JS_HELLO_WORLD       = "print('" + JS_HELLO_PAYLOAD + "');\n'" + JS_HELLO_PAYLOAD + "';";
    public static final String                    PYTHON_HELLO_PAYLOAD = "Hello there from Python";
    public static final String                    PYTHON_HELLO_WORLD   = "print '" + PYTHON_HELLO_PAYLOAD + "'";
    public static final String                    ID_PROJECT_1         = "1";
    public static final String                    SCRIPT_ID_1          = "one";
    public static final String                    SCRIPT_LABEL_1       = "label one";
    public static final String                    SCRIPT_ID_2          = "two";
    public static final String                    SCRIPT_LABEL_2       = "label two";
    public static final String                    SCRIPT_ID_3          = "three";
    public static final String                    SCRIPT_LABEL_3       = "label three";
    public static final String                    EVENT_CLASS          = "org.nrg.xft.event.entities.WorkflowStatusEvent";
    public static final String                    STATUS_COMPLETE      = "Complete";
    public static final Map<String, List<String>> EVENT_FILTERS        = ImmutableMap.<String, List<String>>builder().put("status", Collections.singletonList(STATUS_COMPLETE)).build();

    private final ScriptRunnerService _runnerService;
    private final SerializerService   _serializer;

    @Autowired
    public TestScriptRunnerService(final ScriptRunnerService runnerService, final SerializerService serializer) {
        _runnerService = runnerService;
        _serializer    = serializer;
    }

    @Test
    public void testRunnerSerialization() throws IOException {
        final List<String> runners = _runnerService.getRunners();
        final String       json    = _serializer.toJson(runners);
        assertThat(json).isNotNull().isNotBlank();
    }

    @Test
    public void addRetrieveAndRunSiteScriptTests() throws NrgServiceException {
        _runnerService.setScript(SCRIPT_ID_1, SCRIPT_LABEL_1, GROOVY_HELLO_WORLD, Scope.Site, null, EVENT_CLASS, "EVENT1", EVENT_FILTERS);
        _runnerService.setScript(SCRIPT_ID_2, SCRIPT_LABEL_2, JS_HELLO_WORLD, Scope.Site, null, EVENT_CLASS, "EVENT2", EVENT_FILTERS, "JavaScript");
        _runnerService.setScript(SCRIPT_ID_3, SCRIPT_LABEL_3, PYTHON_HELLO_WORLD, Scope.Site, null, EVENT_CLASS, "EVENT3", EVENT_FILTERS, "Python");

        final Script script1 = _runnerService.getScript(SCRIPT_ID_1);
        final Script script2 = _runnerService.getScript(SCRIPT_ID_2);
        final Script script3 = _runnerService.getScript(SCRIPT_ID_3);
        assertThat(Stream.of(script1, script2, script3)).isNotNull().extracting(Script::getContent, Script::getLanguage).
                                                        contains(tuple(GROOVY_HELLO_WORLD, "groovy"),
                                                                 tuple(JS_HELLO_WORLD, "JavaScript"),
                                                                 tuple(PYTHON_HELLO_WORLD, "Python"));

        final ScriptOutput output1 = _runnerService.runScript(script1);
        final ScriptOutput output2 = _runnerService.runScript(script2);
        final ScriptOutput output3 = _runnerService.runScript(script3);
        assertThat(output1).isNotNull().extracting(ScriptOutput::getResults).isInstanceOf(String.class).isEqualTo(GROOVY_HELLO_PAYLOAD);
        assertThat(output2).isNotNull().extracting(ScriptOutput::getResults).isInstanceOf(String.class).isEqualTo(JS_HELLO_PAYLOAD);
        // Python doesn't output a result the same way other scripting languages do. We have to look at the console instead.
        assertThat(output3).isNotNull().hasFieldOrPropertyWithValue("results", null).extracting(ScriptOutput::getOutput).isEqualTo(PYTHON_HELLO_PAYLOAD);
    }

    @Test
    public void addRetrieveAndRunProjectScriptTest() throws NrgServiceException {
        _runnerService.setScript(SCRIPT_ID_1, SCRIPT_LABEL_1, GROOVY_HELLO_PROJECT, Scope.Project, ID_PROJECT_1);
        final List<Script> scripts = _runnerService.getScripts(Scope.Project, ID_PROJECT_1);
        assertThat(scripts).isNotNull().isNotEmpty().hasSize(1)
                           .extracting(Script::getScriptId, Script::getScriptLabel, Script::getContent)
                           .contains(tuple(SCRIPT_ID_1, SCRIPT_LABEL_1, GROOVY_HELLO_PROJECT));


        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("scope", Scope.Project.code());
        parameters.put("entityId", ID_PROJECT_1);
        parameters.put("event", ScriptTrigger.DEFAULT_EVENT);
        //parameters.put("scriptVersion", "1");
        final ScriptOutput output = _runnerService.runScript(scripts.get(0), parameters);
        assertThat(output).isNotNull()
                          .extracting(ScriptOutput::getOutput)
                          .isInstanceOf(String.class)
                          .isEqualTo("hi there " + Scope.Project.code() + " " + ID_PROJECT_1);
    }

    @Test
    public void useImportedClassTest() throws NrgServiceException {
        _runnerService.setScript(SCRIPT_ID_1, SCRIPT_LABEL_1, GROOVY_IMPORT);
        final Script script = _runnerService.getScript(SCRIPT_ID_1);
        assertThat(script).isNotNull()
                          .hasFieldOrPropertyWithValue("scriptId", SCRIPT_ID_1)
                          .hasFieldOrPropertyWithValue("scriptLabel", SCRIPT_LABEL_1)
                          .hasFieldOrPropertyWithValue("content", GROOVY_IMPORT);

        final ScriptOutput output = _runnerService.runScript(script);
        assertThat(output).isNotNull()
                          .hasFieldOrPropertyWithValue("results", "'hi there' is not blank");
    }

    @Test
    public void returnComplexObject() throws NrgServiceException {
        _runnerService.setScript(SCRIPT_ID_1, SCRIPT_LABEL_1, GROOVY_OBJECT);

        final Script script = _runnerService.getScript(SCRIPT_ID_1);
        assertThat(script).isNotNull()
                          .hasFieldOrPropertyWithValue("scriptId", SCRIPT_ID_1)
                          .hasFieldOrPropertyWithValue("scriptLabel", SCRIPT_LABEL_1)
                          .hasFieldOrPropertyWithValue("content", GROOVY_OBJECT);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("submit", 4);

        final ScriptOutput output = _runnerService.runScript(script, parameters);
        assertThat(output).isNotNull()
                          .extracting(ScriptOutput::getResults, as(InstanceOfAssertFactories.map(String.class, Integer.class)))
                          .contains(entry("one", 1), entry("two", 2), entry("three", 3), entry("four", 4));
    }

    @Test
    public void passVariablesTest() throws NrgServiceException {
        _runnerService.setScript(SCRIPT_ID_1, SCRIPT_LABEL_1, GROOVY_VARIABLE);

        final Script script = _runnerService.getScript(SCRIPT_ID_1);
        assertThat(script).isNotNull()
                          .hasFieldOrPropertyWithValue("scriptId", SCRIPT_ID_1)
                          .hasFieldOrPropertyWithValue("scriptLabel", SCRIPT_LABEL_1)
                          .hasFieldOrPropertyWithValue("content", GROOVY_VARIABLE);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("variable", "This is a value!");

        final ScriptOutput output = _runnerService.runScript(script, parameters);
        assertThat(output).isNotNull()
                          .hasFieldOrPropertyWithValue("results", "This is a value!");
    }

    @Test
    @Disabled("These methods need to be tested, but aren't right now.")
    public void callOtherSetScriptFunctions() {
        final Script                    script       = new Script();
        final String                    scriptId     = "";
        final String                    scriptLabel  = "";
        final String                    content      = "";
        final String                    description  = "";
        final Scope                     scope        = Scope.Project;
        final String                    entityId     = "";
        final String                    eventClass   = "";
        final String                    event        = "";
        final Map<String, List<String>> eventFilters = new HashMap<>();
        _runnerService.setScript(scriptId, scriptLabel, content, description);
        _runnerService.setScript(scriptId, scriptLabel, content, scope, entityId, eventClass, event, eventFilters);
        _runnerService.setScript(scriptId, scriptLabel, content, description, scope, entityId);
        _runnerService.setScript(scriptId, scriptLabel, content, description, scope, entityId, eventClass, event, eventFilters);
        _runnerService.setScript(script, scope, entityId, eventClass, event, eventFilters);
    }
}
