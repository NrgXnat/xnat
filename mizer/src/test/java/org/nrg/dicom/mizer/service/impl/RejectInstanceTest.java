package org.nrg.dicom.mizer.service.impl;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.test.workers.resources.ResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;
import static org.nrg.dicom.mizer.TestUtils.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMizerConfig.class)
public class RejectInstanceTest {
    @Test
    @Ignore
    public void testReject() throws MizerException {
        try {
            final DicomObjectI dicom = DicomObjectFactory.newInstance();
            TestTag t1 = new TestTag(0x00100010, "");
            put(dicom, t1);

            assertEquals(t1.initialValue, dicom.getString(t1.tag));

            String script =
                    "version \"6.6\"\n" +
                            "reject[]\n";

            final Map<String, Object> elements = new HashMap<>();
//            elements.put("project", "XNAT_01");
//            elements.put("subject", "XNAT_01_01");
//            elements.put("modalityLabel", "MR");
            final List<MizerContext> contexts = Arrays.<MizerContext>asList(new MizerContextWithScript(0L, script, elements));
//            final Set<Variable> variables = _service.getReferencedVariables(contexts);
//            assertNotNull(variables);
//            assertEquals(4, variables.size());

            _service.anonymize(dicom, contexts);


            assertNotNull(dicom);
        } catch (Exception e) {
//            fail("Unexpected exception: " + e);
        }
    }

    private static final String SITE_TAG = "DicomEdit 6 site anonymization";
    private static final String PROJ_TAG = "DicomEdit 6 XNAT 01 project anonymization";
    private static final String SCRIPT_SITE = "version \"6.1\"\n" +
            "(0008,0070) := \"" + SITE_TAG + "\"\n" +
            "project != \"Unassigned\" ? (0008,1030) := project\n" +
            "(0008,1030) := project\n" +
            "(0010,0010) := subject";
    private static final String SCRIPT_PROJ = "version \"6.1\"\n" +
            "(0008,0080) := \"" + PROJ_TAG + "\"\n" +
            "session := \"XNAT_01_01_MR1\"\n" +
            "(0008,0070) := modalityLabel\n" +
            "alterPixels[\"rectangle\", \"l=100, t=100, r=200, b=200\", \"solid\", \"v=100\"] \n" +
            "(0010,0020) := session";

    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
    private static final File DICOM_TEST = _resourceManager.getTestResourceFile("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm");

    @Autowired
    private MizerService _service;
}
