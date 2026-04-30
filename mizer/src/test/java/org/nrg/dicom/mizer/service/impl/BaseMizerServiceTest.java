package org.nrg.dicom.mizer.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.Mizer;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.dicom.mizer.service.impl.test.TestMizer;
import org.nrg.test.workers.resources.ResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMizerConfig.class)
public class BaseMizerServiceTest {
    @Autowired
    Mizer _testMizer;

    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
    private static final File DICOM_TEST = _resourceManager.getTestResourceFile("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm");

    private static final List<String> SCRIPT_LIST = Arrays.asList(
            "version \"6.1\"",
            "(0008,0070) := \"Test\"",
            "project != \"Unassigned\" ? (0008,1030) := project",
            "(0008,1030) := project",
            "(0010,0010) := subject");
    private static final String SCRIPT_STRING = String.join("\n", SCRIPT_LIST);
    private static final InputStream SCRIPT_INPUTSTREAM = new ByteArrayInputStream(SCRIPT_STRING.getBytes(StandardCharsets.UTF_8));

    /**
     * Test the MizerService methods. Test that a Mizer is found and called but the test mizer doesn't do any anon.
     *
     * @throws MizerException
     */
    @Test
    public void testService() throws MizerException, IOException {
        List<Mizer> mizers = new ArrayList<>();
        mizers.add(_testMizer);
        MizerService service = new org.nrg.dicom.mizer.service.impl.BaseMizerService(mizers);

        assertArrayEquals(mizers.toArray(new Mizer[0]), service.getMizers().toArray());

        MizerContextWithScript context = new MizerContextWithScript(SCRIPT_LIST);

        assertEquals(_testMizer, service.getMizer(context));

        assertArrayEquals(TestMizer.REFERENCED_VARIABLES.toArray(), service.getReferencedVariables(context).toArray());
        assertArrayEquals(TestMizer.REFERENCED_VARIABLES.toArray(), service.getReferencedVariables(Arrays.asList(context)).toArray());

        assertTrue(service.setContext(context));
        assertTrue(service.removeContext(context));
        // There is no service.getContexts() to find currently set contexts.

        assertArrayEquals(TestMizer.REFERENCED_TAG_PATHS.toArray(), service.getScriptTags(context).toArray());
        assertArrayEquals(TestMizer.REFERENCED_TAG_PATHS.toArray(), service.getScriptTags(Arrays.asList(context)).toArray());

        MizerContextWithScript scriptContext = service.createContext("project", "subject", "session", 1, SCRIPT_STRING, false, false);
        File dicomFile = _resourceManager.getTestResourceFile("dicom/vr.dcm");
        DicomObjectI dobj = DicomObjectFactory.newInstance();
        dobj.read(dicomFile);
        boolean record = false;
        boolean ignoreRejection = false;

        // delegates to TestMizer whose anonymizeImpl() method always returns success with message.
        assertEquals(TestMizer.RESULT_MESSAGE, service.anonymize(dobj, scriptContext).getMessage());
        assertEquals(TestMizer.RESULT_MESSAGE, service.anonymize(dobj, Arrays.asList(scriptContext)).getMessage());

        Properties properties = new Properties();
        // Attempt at adding script via properties doesn't work.
        properties.setProperty("statements", SCRIPT_STRING);
        // TODO: service.anonymize( DicomObjectI dobj, Properties properties, boolean ignoreRejection) fails. Script is not extracted from the properties.
        // Is this used?
//            service.anonymize(dobj, properties, ignoreRejection);

        assertEquals(TestMizer.RESULT_MESSAGE, service.anonymize(dobj, new Properties(), SCRIPT_STRING, ignoreRejection).getMessage());
        assertEquals(TestMizer.RESULT_MESSAGE, service.anonymize(dicomFile, "project", "subject", "session", record, ignoreRejection, 1, SCRIPT_INPUTSTREAM).getMessage());
        assertEquals(TestMizer.RESULT_MESSAGE, service.anonymize(dicomFile, "project", "subject", "session", record, ignoreRejection, 1, SCRIPT_STRING).getMessage());
        assertEquals(TestMizer.RESULT_MESSAGE, service.anonymize(dicomFile, "project", "subject", "session", record, ignoreRejection, 1, SCRIPT_LIST).getMessage());
        SCRIPT_INPUTSTREAM.reset();
        assertEquals(TestMizer.RESULT_MESSAGE, service.anonymize(dicomFile, "project", "subject", "session", record, ignoreRejection, SCRIPT_INPUTSTREAM).getMessage());
        assertEquals(TestMizer.RESULT_MESSAGE, service.anonymize(dicomFile, "project", "subject", "session", record, ignoreRejection, SCRIPT_LIST).getMessage());
        assertEquals(TestMizer.RESULT_MESSAGE, service.anonymize(dicomFile, Arrays.asList(scriptContext)).getMessage());
        assertEquals(TestMizer.RESULT_MESSAGE, service.anonymize(dicomFile, scriptContext).getMessage());
        List<AnonymizationResult> res = service.anonymize(Arrays.asList(dicomFile), "project", "subject", "session", 1, SCRIPT_STRING, record, ignoreRejection);
        assertEquals(TestMizer.RESULT_MESSAGE, res.get(0).getMessage());

    }
}
