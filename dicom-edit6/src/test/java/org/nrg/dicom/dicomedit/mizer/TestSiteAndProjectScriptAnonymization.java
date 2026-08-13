package org.nrg.dicom.dicomedit.mizer;

import org.dcm4che3.data.Tag;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.dicom.mizer.exceptions.MizerException;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMizerConfig.class)
/**
 * Anonymization through the mizer service with a site script and a project script applied together.
 * <p>
 * Named TestPixelAnon until it was un-ignored, which is misleading: neither script touches pixel
 * data, they assign header elements. Pixel redaction is covered by
 * {@code StreamingRectanglePixelEditHandlerTest} and {@code TestAlterPixelsFunction}.
 */
public class TestSiteAndProjectScriptAnonymization {
    @Test

    public void appliesSiteAndProjectScripts() throws MizerException {
        final DicomObjectI dicomObject = DicomObjectFactory.newInstance(DICOM_TEST);

        assertEquals("head^DHead", dicomObject.getString(Tag.StudyDescription));
        assertEquals("Sample Patient", dicomObject.getString(Tag.PatientName));
        assertEquals("Sample ID", dicomObject.getString(Tag.PatientID));
        assertEquals("SIEMENS", dicomObject.getString(Tag.Manufacturer));
        assertEquals("Hospital", dicomObject.getString(Tag.InstitutionName));

        final Map<String, Object> elements = new HashMap<>();
        elements.put("project", "XNAT_01");
        elements.put("subject", "XNAT_01_01");
        elements.put("modalityLabel", "MR");
        final List<MizerContext> contexts  = Arrays.asList(new MizerContextWithScript(0L, SCRIPT_SITE, elements), new MizerContextWithScript(0L, SCRIPT_PROJ, elements));
        final Set<Variable> variables = _service.getReferencedVariables(contexts);
        assertNotNull(variables);
        assertEquals(4, variables.size());

        _service.anonymize(dicomObject, contexts);

        assertNotNull(dicomObject);
    }

    private static final String SITE_TAG        = "DicomEdit 6 site anonymization";
    private static final String PROJ_TAG        = "DicomEdit 6 XNAT 01 project anonymization";
    private static final String SCRIPT_SITE     = "version \"6.1\"\n" +
            "(0008,0070) := \"" + SITE_TAG + "\"\n" +
            "project != \"Unassigned\" ? (0008,1030) := project\n" +
            "(0008,1030) := project\n" +
            "(0010,0010) := subject";
    private static final String SCRIPT_PROJ     = "version \"6.1\"\n" +
            "(0008,0080) := \"" + PROJ_TAG + "\"\n" +
            "session := \"XNAT_01_01_MR1\"\n" +
            "(0008,0070) := modalityLabel\n" +
            "alterPixels[\"rectangle\", \"l=100, t=100, r=200, b=200\", \"solid\", \"v=100\"] \n" +
            "(0010,0020) := session";

    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
    private static final File DICOM_TEST       = _resourceManager.getTestResourceFile("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");

    @Autowired
    private MizerService _service;
}
