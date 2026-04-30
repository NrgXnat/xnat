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
import org.nrg.dicom.mizer.objects.AnonymizationResultSeverity;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.variables.BasicVariable;
import org.nrg.dicom.mizer.variables.Variable;
import org.nrg.test.workers.resources.ResourceManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Run tests of BaseScriptApplicator methods.
 */
public class SerialScriptApplicatorTests {
    private static final ResourceManager _resourceManager = ResourceManager.getInstance();

    private static final File DICOM_FILE = _resourceManager.getTestResourceFile("dicom/mellon.dcm");
    private static final List<String> SCRIPT1_LIST = Arrays.asList(
            "version \"6.1\"",
            "scriptVariableName1 := \"scriptVariableValue1\"",
            "(0008,0070) := \"Test\"");
    private static final List<String> SCRIPT2_LIST = Arrays.asList(
            "version \"6.1\"",
            "scriptVariableName2 := \"scriptVariableValue2\"",
            "(0010,0020) := \"Test\"");

    @Test
    public void testSerialScriptApplicator() throws MizerException, IOException {
        final List<DE6Script> scriptList = Arrays.asList(
                (new DE6Script.Builder()).statements(SCRIPT1_LIST).build(),
                (new DE6Script.Builder()).statements(SCRIPT2_LIST).build() );

        SerialScriptApplicator applicator = new SerialScriptApplicator(scriptList);
        assertEquals(AnonymizationResultSeverity.SUCCESS, applicator.apply(DICOM_FILE).getSeverity());
        assertEquals(AnonymizationResultSeverity.SUCCESS, applicator.apply(new BufferedInputStream(Files.newInputStream(DICOM_FILE.toPath()))).getSeverity());
        assertEquals(AnonymizationResultSeverity.SUCCESS, applicator.apply(DicomObjectFactory.newInstance()).getSeverity());
        // returns void
        applicator.apply(new File("matchfile"), DicomObjectFactory.newInstance());

        Variable scriptVariable1 = new BasicVariable("scriptVariableName1", "scriptVariableValue1");
        Variable scriptVariable2 = new BasicVariable("scriptVariableName2", "scriptVariableValue2");
        assertEquals(scriptVariable1.getValue().asString(), applicator.getVariable("scriptVariableName1").getValue().asString());
        Variable setVariable = new BasicVariable("setVariableName", "setVariableValue");
        applicator.setVariable(setVariable.getName(), setVariable.getValue().asString());
        assertEquals(setVariable.getValue().asString(), applicator.getVariable(setVariable.getName()).getValue().asString());
        // TODO: shouldn't this return the set variable too?
        assertArrayEquals(new String[]{"scriptVariableName1","scriptVariableName2"}, applicator.getVariableNames().toArray());
        // This returns both the script and set variable.
        assertArrayEquals(new Variable[]{scriptVariable1, scriptVariable2, setVariable}, applicator.getVariables().values().toArray());
        // No external variables. How do I make one?
        assertArrayEquals(new String[]{}, applicator.getExternalVariableNames().toArray());
        // get the value of the named variable.
        assertEquals(scriptVariable1.getValue().asString(), applicator.getValue(scriptVariable1.getName()).asString());
        assertEquals(0x7FE0000F, applicator.getTopTag());
    }
}
