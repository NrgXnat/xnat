package org.nrg.dicom.mizer.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.Mizer;
import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.dicom.mizer.service.impl.test.TestMizer;
import org.nrg.test.workers.resources.ResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.nrg.dicom.mizer.TestUtils.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMizerConfig.class)
public class AbstractMizerTest {
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

    @Test
    public void testAbstractMizer() throws MizerException {
        AbstractMizer abstractMizer = (AbstractMizer) _testMizer;
        MizerContext context = new MizerContextWithScript(SCRIPT_LIST);
        assertTrue(abstractMizer.understands(context));
        assertTrue(abstractMizer.getMaxSupportedVersion().matches("6.1"));
        assertEquals(0, abstractMizer.compareTo(abstractMizer));

        DicomObjectI dobj = DicomObjectFactory.newInstance();
        TestSeqTag deIDMethodCodeValue = new TestSeqTag(new int[]{0x00120064, 0, 0x00080100}, "1");
        TestSeqTag deIDMethodCodeSchemeDesignator = new TestSeqTag(new int[]{0x00120064, 0, 0x00080102}, TestMizer.CODE_DESIGNATOR);
        TestSeqTag deIDMethodCodeSchemeVersion = new TestSeqTag(new int[]{0x00120064, 0, 0x00080103}, TestMizer.CODE_VERSION);
        TestSeqTag deIDMethodCodeSchemeMeaning = new TestSeqTag(new int[]{0x00120064, 0, 0x00080104}, TestMizer.CODE_MEANING);
        assertFalse(dobj.contains(deIDMethodCodeValue.tag));
        assertFalse(dobj.contains(deIDMethodCodeSchemeDesignator.tag));
        assertFalse(dobj.contains(deIDMethodCodeSchemeVersion.tag));
        assertFalse(dobj.contains(deIDMethodCodeSchemeMeaning.tag));
        abstractMizer.addRecord(dobj, Integer.parseInt(deIDMethodCodeValue.initialValue));
        assertEquals(deIDMethodCodeValue.initialValue, dobj.getString(deIDMethodCodeValue.tag));
        assertEquals(deIDMethodCodeSchemeDesignator.initialValue, dobj.getString(deIDMethodCodeSchemeDesignator.tag));
        assertEquals(deIDMethodCodeSchemeVersion.initialValue, dobj.getString(deIDMethodCodeSchemeVersion.tag));
        assertEquals(deIDMethodCodeSchemeMeaning.initialValue, dobj.getString(deIDMethodCodeSchemeMeaning.tag));

        deIDMethodCodeValue = new TestSeqTag(new int[]{0x00120064, 1, 0x00080100}, "2");
        deIDMethodCodeSchemeDesignator = new TestSeqTag(new int[]{0x00120064, 1, 0x00080102}, "schemeDesignator");
        deIDMethodCodeSchemeVersion = new TestSeqTag(new int[]{0x00120064, 1, 0x00080103}, "schemeVersion");
        deIDMethodCodeSchemeMeaning = new TestSeqTag(new int[]{0x00120064, 1, 0x00080104}, "schemeMeaning");
        assertFalse(dobj.contains(deIDMethodCodeValue.tag));
        assertFalse(dobj.contains(deIDMethodCodeSchemeDesignator.tag));
        assertFalse(dobj.contains(deIDMethodCodeSchemeVersion.tag));
        assertFalse(dobj.contains(deIDMethodCodeSchemeMeaning.tag));
        abstractMizer.addRecord(dobj, Integer.parseInt(deIDMethodCodeValue.initialValue), deIDMethodCodeSchemeMeaning.initialValue, deIDMethodCodeSchemeDesignator.initialValue, deIDMethodCodeSchemeVersion.initialValue);
        assertEquals(deIDMethodCodeValue.initialValue, dobj.getString(deIDMethodCodeValue.tag));
        assertEquals(deIDMethodCodeSchemeDesignator.initialValue, dobj.getString(deIDMethodCodeSchemeDesignator.tag));
        assertEquals(deIDMethodCodeSchemeVersion.initialValue, dobj.getString(deIDMethodCodeSchemeVersion.tag));
        assertEquals(deIDMethodCodeSchemeMeaning.initialValue, dobj.getString(deIDMethodCodeSchemeMeaning.tag));
    }
}
