package org.nrg.dicom.dicomedit.mizer;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
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
public class DE6MizerTest {
    @Test
    public void testMultiscriptResolution() throws MizerException {
        final DicomObjectI dicom          = DicomObjectFactory.newInstance(DICOM_TEST);
        final DicomObject dcm4che2Object = dicom.getDcm4che2Object();

        assertEquals("head^DHead", dcm4che2Object.getString(Tag.StudyDescription));
        assertEquals("Sample Patient", dcm4che2Object.getString(Tag.PatientName));
        assertEquals("Sample ID", dcm4che2Object.getString(Tag.PatientID));
        assertEquals("SIEMENS", dcm4che2Object.getString(Tag.Manufacturer));
        assertEquals("Hospital", dcm4che2Object.getString(Tag.InstitutionName));

        final Map<String, Object> elements = new HashMap<>();
        elements.put("project", "XNAT_01");
        elements.put("subject", "XNAT_01_01");
        elements.put("modalityLabel", "MR");
        final List<MizerContext> contexts  = Arrays.<MizerContext>asList(new MizerContextWithScript(0L, SCRIPT_SITE, elements), new MizerContextWithScript(0L, SCRIPT_PROJ, elements));
        final Set<Variable> variables = _service.getReferencedVariables(contexts);
        assertNotNull(variables);
        assertEquals(4, variables.size());

        _service.anonymize(dicom, contexts);

        assertNotNull(dicom);
    }

    @Test
    public void testInlineCommentsInArrayDefinition() throws MizerException {
        final Map<String, Object> elements = new HashMap<>();
        elements.put("project", "XNAT_01");
        elements.put("subject", "XNAT_01_01");
        elements.put("modalityLabel", "MR");
        final List<MizerContext> contexts  = Collections.singletonList(new MizerContextWithScript(0L, SCRIPT_INLINE_COMMENTS, elements));
        final Set<Variable> variables = _service.getReferencedVariables(contexts);
        assertNotNull(variables);
        assertEquals(4, variables.size());
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
            "(0010,0020) := session";
    private static final String SCRIPT_INLINE_COMMENTS = "version \"6.6\"\n" +
                                                         "(0008,1030) := project\n" +
                                                         "(0010,0020) := subject\n" +
                                                         "(0010,0030) := modalityLabel\n" +
                                                         "tagsToRemove := {\n" +
                                                         "    (0008,0070),  // Manufacturer\n" +
                                                         "    (0008,0080),  // InstitutionName\n" +
                                                         "    (0008,0081),  // InstitutionAddress\n" +
                                                         "    (0008,1010),  // StationName\n" +
                                                         "    (0008,1090),  // ManufacturerModelName\n" +
                                                         "    (0010,0030)   // PatientBirthDate\n" +
                                                         "}\n" +
                                                         "removeTags[tagsToRemove]\n";

    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
    private static final File DICOM_TEST       = _resourceManager.getTestResourceFile("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");

    @Autowired
    private MizerService _service;
}
