package org.nrg.dicom.dicomedit.mizer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultReject;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.nrg.dicom.mizer.variables.Variable;
import org.nrg.test.workers.resources.ResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMizerConfig.class)
public class TestRejectInstance {
    @Test
    public void testRejectInstance() throws MizerException {
        final DicomObjectI dicom          = DicomObjectFactory.newInstance(DICOM_TEST);

        final Map<String, Object> elements = new HashMap<>();
        elements.put("project", "XNAT_01");
        elements.put("subject", "XNAT_01_01");
        elements.put("modalityLabel", "MR");
        MizerContextWithScript context = new MizerContextWithScript(0L, SCRIPT_PROJ, elements);
        context.setIgnoreRejection(false);
//        context.setElement("reject:isEnabled", "false");
//        context.setElement("reject:isWarnRejectionEnabled", "true");
        final List<MizerContext> contexts  = Arrays.<MizerContext>asList( context);
        final Set<Variable> variables = _service.getReferencedVariables(contexts);
        assertNotNull(variables);
        assertEquals(1, variables.size());

        AnonymizationResult result = _service.anonymize(dicom, contexts);

        assertTrue( result instanceof AnonymizationResultReject);
    }

    @Test
    public void testDoNotRejectInstance() throws MizerException {
        final DicomObjectI dicom          = DicomObjectFactory.newInstance(DICOM_TEST);

        final Map<String, Object> elements = new HashMap<>();
        elements.put("project", "XNAT_01");
        elements.put("subject", "XNAT_01_01");
        elements.put("modalityLabel", "MR");
        MizerContextWithScript context = new MizerContextWithScript(0L, SCRIPT_PROJ, elements);
        context.setIgnoreRejection(true);
//        context.setElement("reject:isEnabled", "false");
//        context.setElement("reject:isWarnRejectionEnabled", "true");
        final List<MizerContext> contexts  = Arrays.<MizerContext>asList( context);
        final Set<Variable> variables = _service.getReferencedVariables(contexts);
        assertNotNull(variables);
        assertEquals(1, variables.size());

        AnonymizationResult result = _service.anonymize(dicom, contexts);

        assertFalse( result instanceof AnonymizationResultReject);
    }

    private static final String PROJ_TAG        = "DicomEdit 6 XNAT 01 project anonymization";
    private static final String SCRIPT_PROJ     =
            "version \"6.6\"\n" +
            "(0008,0080) := \"" + PROJ_TAG + "\"\n" +
            "session := \"XNAT_01_01_MR1\"\n" +
            "reject[]\n" +
            "(0010,0020) := session";

    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
    private static final File DICOM_TEST       = _resourceManager.getTestResourceFile("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");

    @Autowired
    private MizerService _service;
}
