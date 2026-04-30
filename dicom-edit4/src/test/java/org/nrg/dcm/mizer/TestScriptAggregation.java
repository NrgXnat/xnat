/*
 * dicom-edit4: org.nrg.dcm.mizer.TestScriptAggregation
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.mizer;

import org.dcm4che3.data.Tag;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings("Duplicates")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMizerConfig.class)
public class TestScriptAggregation {
    @Test
    public void testMultiscriptResolution() throws MizerException {
        final DicomObjectI dicom          = DicomObjectFactory.newInstance(DICOM_TEST);

        assertEquals("head^DHead", dicom.getString(Tag.StudyDescription));
        assertEquals("Sample Patient", dicom.getString(Tag.PatientName));
        assertEquals("Sample ID", dicom.getString(Tag.PatientID));
        assertEquals("SIEMENS", dicom.getString(Tag.Manufacturer));
        assertEquals("Hospital", dicom.getString(Tag.InstitutionName));

        final Map<String, Object> elements = new HashMap<>();
        elements.put("project", "XNAT_01");
        elements.put("subject", "XNAT_01_01");
        elements.put("modalityLabel", "MR");
        final List<MizerContext> contexts  = Arrays.<MizerContext>asList(new MizerContextWithScript(0L, SCRIPT_SITE, elements), new MizerContextWithScript(0L, SCRIPT_PROJ, elements));
        final Set<Variable>      variables = _service.getReferencedVariables(contexts);
        assertNotNull(variables);
        assertEquals(4, variables.size());

        _service.anonymize(dicom, contexts);

        assertNotNull(dicom);
    }

    @Test
    public void testSimpleScriptForXLectricValidation() throws IOException, MizerException {
        final Path temp = Files.createTempDirectory("xlectricTest");
        temp.toFile().deleteOnExit();

        final String originalValue = DicomObjectFactory.newInstance(XLECTRIC_TEST).getString(0x80070);
        final Path target = Files.copy(XLECTRIC_TEST.toPath(), temp.resolve(XLECTRIC_TEST.getName()));

        final MizerContextWithScript context = new MizerContextWithScript();
        context.setScript(XLECTRIC_SCRIPT);

        _service.anonymize(target.toFile(), context);

        final String modifiedValue = DicomObjectFactory.newInstance(target.toFile()).getString(0x80070);
        assertNotNull(originalValue);
        assertEquals("Original value", originalValue);
        assertNotNull(modifiedValue);
        assertEquals("Modified value", modifiedValue);
    }

    private static final String XLECTRIC_SCRIPT = "(0008,0070) := \"Modified value\"";
    private static final String SITE_TAG        = "DicomEdit 4 site anonymization";
    private static final String PROJ_TAG        = "DicomEdit 4 XNAT 01 project anonymization";
    private static final String SCRIPT_SITE     = "(0008,0070) := \"" + SITE_TAG + "\"\n"
                                                  + "project = \"Unassigned\" : (0008,1030) := (0008,1030)\n"
                                                  + "(0008,1030) := project\n"
                                                  + "(0010,0010) := subject";
    private static final String SCRIPT_PROJ     = "(0008,0080) := \"" + PROJ_TAG + "\"\n"
                                                  + "session := makeSessionLabel[format[\"{0}_v##_{1}\", subject, lowercase[modalityLabel]]]\n"
                                                  + "(0010,0020) := session";

    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
    private static final File            DICOM_TEST       = _resourceManager.getTestResourceFile("dicom/1.MR.head_DHead.4.23.20061214.091206.156000.8215318065.dcm");
    private static final File            XLECTRIC_TEST    = _resourceManager.getTestResourceFile("dicom/1.2.840.113654.2.45.2.108105-1-1-2z65o7.dcm");

    @Autowired
    private MizerService _service;
}
