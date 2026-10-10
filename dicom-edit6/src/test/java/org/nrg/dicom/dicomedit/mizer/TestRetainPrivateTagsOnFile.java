package org.nrg.dicom.dicomedit.mizer;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * retainPrivateTags on a file, through the service call that anonymization at archive time makes. The service reads
 * the file with {@code AnonymizeCallOnFileWithPixels}, which leaves bulk data on disk, and writes the result back.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMizerConfig.class)
public class TestRetainPrivateTagsOnFile {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * A private element with an empty binary value does not fail anonymization. The private sequence follows Siemens
     * MEDCOM Application Header Sequence, whose items declare their own creator and carry an OB Application Header
     * Info.
     */
    @Test
    public void emptyPrivateBinaryValueInSequenceItem() throws Exception {
        final Attributes dataset = new Attributes();
        dataset.setString(Tag.SOPClassUID, VR.UI, UID.MRImageStorage);
        dataset.setString(Tag.SOPInstanceUID, VR.UI, "1.2.3.4");
        dataset.setString(Tag.PatientName, VR.PN, "t1");
        dataset.setString(0x00290010, VR.LO, "SIEMENS MEDCOM HEADER");
        final Attributes item = new Attributes();
        item.setString(0x00290010, VR.LO, "SIEMENS MEDCOM HEADER");
        item.setString(0x00291041, VR.CS, "SOM 5 TIMESTAMPS");
        item.setBytes(0x00291044, VR.OB, new byte[0]);
        dataset.newSequence(0x00291040, 1).add(item);

        final File file = temporaryFolder.newFile("empty-ob.dcm");
        try (DicomOutputStream output = new DicomOutputStream(file)) {
            output.writeDataset(dataset.createFileMetaInformation(UID.ExplicitVRLittleEndian), dataset);
        }

        final AnonymizationResult result = _service.anonymize(file, new MizerContextWithScript(0L, SCRIPT, new HashMap<>()));
        assertFalse(result.getMessage(), result instanceof AnonymizationResultError);

        final Attributes retained = datasetOf(file).getNestedDataset(0x00291040);
        assertNotNull(retained);
        assertEquals("SOM 5 TIMESTAMPS", retained.getString(0x00291041));
        assertEquals(0, retained.getBytes(0x00291044).length);
    }

    private static Attributes datasetOf(final File file) throws IOException {
        try (DicomInputStream input = new DicomInputStream(file)) {
            input.readFileMetaInformation();
            return input.readDataset();
        }
    }

    private static final String SCRIPT = "version \"6.6\"\n" +
                                         "retainPrivateTags[ (0029,{SIEMENS MEDCOM HEADER}XX)]\n";

    @Autowired
    private MizerService _service;
}
