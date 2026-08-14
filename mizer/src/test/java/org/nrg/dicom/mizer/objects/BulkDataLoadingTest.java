package org.nrg.dicom.mizer.objects;

import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.MessageDigest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Bulk data stays on disk when asked for by reference, and comes back byte for byte on write.
 * <p>
 * This is the behaviour the whole large-object fix rests on, and until this test existed nothing in
 * the default suite covered it: replacing the requested mode with
 * {@link DicomInputStream.IncludeBulkData#YES} broke no test at all. {@code LargeDicomObjectTest}
 * does catch it, by being unable to load its object at all, but it is opt-in and costs several GB,
 * so it cannot be the only guard.
 */
public class BulkDataLoadingTest {

    // Must be an object that actually has pixel data. vr.dcm does not, and with no bulk data to
    // hold either way, the round-trip test below passes without proving anything.
    private static final String FIXTURE = "dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm";

    @Test
    public void keepsBulkDataOnDiskWhenAskedForByReference() throws Exception {
        final DicomObjectI dicomObject =
                DicomObjectFactory.newInstance(fixture(), DicomInputStream.IncludeBulkData.URI);

        final Object value = dicomObject.getAttributes().getValue(Tag.PixelData);
        assertTrue("pixel data should be a reference, not a value on the heap: "
                   + (value == null ? "null" : value.getClass().getName()),
                   value instanceof BulkData);
        assertEquals("the reference should point at the file it was read from",
                     fixture().getCanonicalFile(), ((BulkData) value).getFile().getCanonicalFile());
    }

    @Test
    public void readsBulkDataOntoTheHeapWhenAskedFor() throws Exception {
        final DicomObjectI dicomObject =
                DicomObjectFactory.newInstance(fixture(), DicomInputStream.IncludeBulkData.YES);

        final Object value = dicomObject.getAttributes().getValue(Tag.PixelData);
        assertTrue("pixel data should have been read onto the heap: "
                   + (value == null ? "null" : value.getClass().getName()),
                   value instanceof byte[]);
    }

    /**
     * The point of holding a reference rather than a value: what is written has to be identical to
     * what was read, having never been on the heap in between.
     */
    @Test
    public void streamsReferencedBulkDataThroughToTheOutput() throws Exception {
        final DicomObjectI byReference =
                DicomObjectFactory.newInstance(fixture(), DicomInputStream.IncludeBulkData.URI);
        assertTrue("the fixture has no bulk data, so this would compare two objects that never had any",
                   byReference.getAttributes().getValue(Tag.PixelData) instanceof BulkData);
        final DicomObjectI onHeap =
                DicomObjectFactory.newInstance(fixture(), DicomInputStream.IncludeBulkData.YES);

        final File fromReference = write(byReference);
        final File fromHeap      = write(onHeap);
        try {
            assertArrayEquals("writing referenced bulk data should produce the same bytes as writing "
                              + "it from the heap", digest(fromHeap), digest(fromReference));
        } finally {
            Files.deleteIfExists(fromReference.toPath());
            Files.deleteIfExists(fromHeap.toPath());
        }
    }

    private static File write(final DicomObjectI dicomObject) throws MizerException, java.io.IOException {
        final File out = File.createTempFile("bulkdata", ".dcm");
        try (OutputStream stream = new FileOutputStream(out)) {
            dicomObject.write(stream);
        }
        dicomObject.releaseScratchFiles();
        return out;
    }

    private static byte[] digest(final File file) throws Exception {
        return MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file.toPath()));
    }

    private static File fixture() throws Exception {
        return new File(BulkDataLoadingTest.class.getClassLoader().getResource(FIXTURE).toURI());
    }
}
