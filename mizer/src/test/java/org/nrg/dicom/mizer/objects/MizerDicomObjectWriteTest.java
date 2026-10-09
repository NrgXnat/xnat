package org.nrg.dicom.mizer.objects;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link DicomObjectI#write} buffers the stream it is given. That must not change a single byte of
 * what is written, so the output is compared with dcm4che writing the same attributes directly.
 */
public class MizerDicomObjectWriteTest {

    private static final String FIXTURE = "dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

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

    /**
     * A Deflated-transfer-syntax file read with {@link DicomInputStream.IncludeBulkData#URI} must
     * not reference pixel data into the raw file: positions in an inflated stream are not file
     * positions, so dcm4che spools in that case and so must we. Regression test for the merge-time
     * anonymization of Deflated objects, which read files exactly this way.
     */
    @Test
    public void deflatedFileRoundTripsPixelsThroughUriRead() throws Exception {
        final File   deflated = deflate(fixture());
        final byte[] before   = pixels(readWhole(deflated));

        final ByteArrayOutputStream written = new ByteArrayOutputStream();
        final DicomObjectI read = DicomObjectFactory.newInstance(deflated, DicomInputStream.IncludeBulkData.URI);
        try {
            read.write(written);
        } finally {
            read.releaseScratchFiles();
        }

        assertArrayEquals("pixel data must survive a URI read of a Deflated file",
                          before, pixels(readWhole(new ByteArrayInputStream(written.toByteArray()))));
    }

    private File deflate(final File source) throws Exception {
        final Attributes dataset = readWhole(source);
        final File deflated = folder.newFile("deflated.dcm");
        try (DicomOutputStream out = new DicomOutputStream(deflated)) {
            out.writeDataset(dataset.createFileMetaInformation(UID.DeflatedExplicitVRLittleEndian), dataset);
        }
        return deflated;
    }

    private static Attributes readWhole(final File file) throws Exception {
        try (DicomInputStream in = new DicomInputStream(file)) {
            in.readFileMetaInformation();
            return in.readDataset();
        }
    }

    private static Attributes readWhole(final ByteArrayInputStream bytes) throws Exception {
        try (DicomInputStream in = new DicomInputStream(bytes)) {
            in.readFileMetaInformation();
            return in.readDataset();
        }
    }

    private static byte[] pixels(final Attributes dataset) throws Exception {
        final byte[] pixels = dataset.getBytes(Tag.PixelData);
        assertNotNull("the fixture must carry pixel data for this test to mean anything", pixels);
        return pixels;
    }

    private static File fixture() throws Exception {
        return new File(MizerDicomObjectWriteTest.class.getClassLoader().getResource(FIXTURE).toURI());
    }
}
