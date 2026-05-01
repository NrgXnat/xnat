package org.nrg.dicom.dicomedit.mizer;

import org.dcm4che2.data.DicomObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.nrg.test.workers.resources.ResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMizerConfig.class)
public class VersionStringTest {

    @Test
    public void testKnownVersion() throws MizerException {
        final DicomObjectI dicom          = DicomObjectFactory.newInstance(DICOM_TEST);
        final DicomObject dobj = dicom.getDcm4che2Object();

        assertNull( dobj.getString( 0x00100666));

        final Map<String, Object> elements = new HashMap<>();
        final List<MizerContext> contexts  = Arrays.<MizerContext>asList(new MizerContextWithScript(0L, SCRIPT_V1, elements));

        final AnonymizationResult result = _service.anonymize(dicom, contexts);

        assertEquals( "fubar", dobj.getString( 0x00100666));
    }

    @Test
    public void testUnKnownVersion() {
        try {
            final DicomObjectI dicom = DicomObjectFactory.newInstance(DICOM_TEST);
            final DicomObject dobj = dicom.getDcm4che2Object();

            assertNull(dobj.getString(0x00100666));

            final Map<String, Object> elements = new HashMap<>();
            final List<MizerContext> contexts = Arrays.<MizerContext>asList(new MizerContextWithScript(0L, SCRIPT_VUNK, elements));

            final AnonymizationResult result = _service.anonymize(dicom, contexts);
            assertTrue(result instanceof AnonymizationResultError);
            assertTrue(String.join("\n",result.getMessages()).contains("Unsupported version"));
        }
        catch( MizerException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    private static final String SCRIPT_V1 =
            "version \"6.7\"\n" +
                    "(0010,0666) := \"fubar\"\n";
    private static final String SCRIPT_VUNK =
            "version \"6\"\n" +
                    "(0010,0666) := \"fubar\"\n";

    private static final ResourceManager _resourceManager = ResourceManager.getInstance();
    private static final File DICOM_TEST = _resourceManager.getTestResourceFile("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm.gz");

    @Autowired
    private MizerService _service;
}
