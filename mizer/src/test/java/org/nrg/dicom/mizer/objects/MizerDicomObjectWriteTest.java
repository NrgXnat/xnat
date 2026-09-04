package org.nrg.dicom.mizer.objects;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link DicomObjectI#write} buffers the stream it is given. That must not change a single byte of
 * what is written, so the output is compared with dcm4che writing the same attributes directly.
 */
public class MizerDicomObjectWriteTest {

    private static final String FIXTURE = "dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm";

    @Test
    public void writesExactlyWhatDcm4cheWouldWrite() throws Exception {
        final File file = fixture();

        final ByteArrayOutputStream reference = new ByteArrayOutputStream();
        try (DicomInputStream in = new DicomInputStream(file)) {
            final Attributes fmi     = in.readFileMetaInformation();
            final Attributes dataset = in.readDataset();
            try (DicomOutputStream out = new DicomOutputStream(reference, UID.ExplicitVRLittleEndian)) {
                out.writeDataset(fmi, dataset);
            }
        }

        final ByteArrayOutputStream written = new ByteArrayOutputStream();
        DicomObjectFactory.newInstance(file, DicomInputStream.IncludeBulkData.URI).write(written);

        assertTrue("the fixture should be large enough to span many buffer flushes", reference.size() > 100_000);
        assertArrayEquals(reference.toByteArray(), written.toByteArray());
    }

    private static File fixture() throws Exception {
        return new File(MizerDicomObjectWriteTest.class.getClassLoader().getResource(FIXTURE).toURI());
    }
}
