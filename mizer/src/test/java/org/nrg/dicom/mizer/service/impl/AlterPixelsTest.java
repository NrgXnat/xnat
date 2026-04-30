package org.nrg.dicom.mizer.service.impl;

import org.dcm4che3.data.Tag;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.dicom.mizer.variables.Variable;
import org.nrg.test.workers.resources.ResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMizerConfig.class)
public class AlterPixelsTest {
    @Test
    @Ignore
    public void testMizerAlterPixels() throws MizerException {
        try {
            final File testFile = File.createTempFile("mizer.", ".dcm");
            Files.copy(DICOM_TEST.toPath(), testFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            final DicomObjectI dcm4che2Object = DicomObjectFactory.newInstance(testFile);

            assertEquals("head^DHead", dcm4che2Object.getString(Tag.StudyDescription));
            assertEquals("Sample Patient", dcm4che2Object.getString(Tag.PatientName));
            assertEquals("Sample ID", dcm4che2Object.getString(Tag.PatientID));
            assertEquals("SIEMENS", dcm4che2Object.getString(Tag.Manufacturer));
            assertEquals("Hospital", dcm4che2Object.getString(Tag.InstitutionName));
            assertTrue(dcm4che2Object.contains(Tag.PixelData));

            final Map<String, Object> elements = new HashMap<>();
            elements.put("project", "XNAT_01");
            elements.put("subject", "XNAT_01_01");
            elements.put("modalityLabel", "MR");
            final List<MizerContext> contexts = Arrays.<MizerContext>asList(new MizerContextWithScript(0L, SCRIPT_SITE, elements), new MizerContextWithScript(1L, SCRIPT_PROJ, elements));
            final Set<Variable> variables = _service.getReferencedVariables(contexts);
            assertNotNull(variables);
            assertEquals(4, variables.size());

            _service.anonymize(testFile, contexts);

            System.out.println("Currently have to open file in image viewer to confirm pixel edits. " + testFile.getAbsolutePath());
            // TODO: currently have to open the tempFile in an image viewer and confirm the pixel edits.
        } catch (IOException e) {
            fail("Unexpected exception: " + e);
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
