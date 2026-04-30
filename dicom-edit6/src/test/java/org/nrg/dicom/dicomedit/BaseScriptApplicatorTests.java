/*
 * DicomEdit: TestFunctions
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.*;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.nrg.dicom.mizer.variables.BasicVariable;
import org.nrg.dicom.mizer.variables.Variable;
import org.nrg.test.workers.resources.ResourceManager;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.nrg.dicom.dicomedit.TestUtils.bytes;

/**
 * Run tests of BaseScriptApplicator methods.
 */
public class BaseScriptApplicatorTests {
    private static final ResourceManager _resourceManager = ResourceManager.getInstance();

    private static final File DICOM_FILE = _resourceManager.getTestResourceFile("dicom/mellon.dcm");
    private static final List<String> SCRIPT_LIST = Arrays.asList(
            "version \"6.1\"",
            "scriptVariableName := \"scriptVariableValue\"",
            "(0008,0070) := \"Test\"");
    private static final String SCRIPT_STRING = String.join("\n", SCRIPT_LIST);

    @Test
    public void testBaseScriptApplicator() throws MizerException, IOException {
        MizerContextWithScript context = new MizerContextWithScript(SCRIPT_LIST);
        BaseScriptApplicator applicator = BaseScriptApplicator.getInstance(context);
        assertEquals("Success", applicator.apply(DICOM_FILE).getMessage());
        assertEquals("Success", applicator.apply(new BufferedInputStream(Files.newInputStream(DICOM_FILE.toPath()))).getMessage());
        assertEquals("Success", applicator.apply(DicomObjectFactory.newInstance()).getMessage());
        // returns void
        applicator.apply(new File("matchfile"), DicomObjectFactory.newInstance());

        Variable scriptVariable = new BasicVariable("scriptVariableName", "scriptVariableValue");
        assertEquals(scriptVariable.getValue().asString(), applicator.getVariable("scriptVariableName").getValue().asString());
        Variable setVariable = new BasicVariable("setVariableName", "setVariableValue");
        applicator.setVariable(setVariable.getName(), setVariable.getValue().asString());
        assertEquals(setVariable.getValue().asString(), applicator.getVariable(setVariable.getName()).getValue().asString());
        // TODO: shouldn't this return the set variable too?
        assertArrayEquals(new String[]{scriptVariable.getName()}, applicator.getVariableNames().toArray());
        // This returns both the script and set variable.
        assertArrayEquals(new Variable[]{scriptVariable, setVariable}, applicator.getVariables().values().toArray());
        // No external variables. How do I make one?
        assertArrayEquals(new String[]{}, applicator.getExternalVariableNames().toArray());
        // get the value of the named variable.
        assertEquals(scriptVariable.getValue().asString(), applicator.getValue(scriptVariable.getName()).asString());
        assertEquals(0x7FE0000F, applicator.getTopTag());

        boolean ignoreRejections = false;
        DE6Script script = new DE6Script.Builder().statements(SCRIPT_LIST).build();
        applicator = BaseScriptApplicator.getInstance(script);
        assertEquals(scriptVariable.getName(), applicator.getVariable("scriptVariableName").getName());
        applicator = BaseScriptApplicator.getInstance(bytes(SCRIPT_STRING));
        assertEquals(scriptVariable.getName(), applicator.getVariable("scriptVariableName").getName());
        applicator = BaseScriptApplicator.getInstance(script, ignoreRejections);
        assertEquals(scriptVariable.getName(), applicator.getVariable("scriptVariableName").getName());
        applicator = BaseScriptApplicator.getInstance(bytes(SCRIPT_STRING), ignoreRejections);
        assertEquals(scriptVariable.getName(), applicator.getVariable("scriptVariableName").getName());
    }
}
