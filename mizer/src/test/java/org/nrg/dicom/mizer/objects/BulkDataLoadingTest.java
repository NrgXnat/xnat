package org.nrg.dicom.mizer.objects;

import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nrg.dicom.mizer.exceptions.MizerException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

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

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

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

    /**
     * A gzipped source cannot hold bulk data by reference: the offsets a
     * {@link BulkData} carries are into the decompressed stream and bear no relation to positions
     * in the file. dcm4che detects that and spools the bulk data to temporary files of its own
     * instead, which nothing else will clean up -- one file the size of the pixel data, per object.
     */
    @Test
    public void deletesTheFilesDcm4cheSpoolsForAGzippedSource() throws Exception {
        final File gzipped = temporaryFolder.newFile("gzipped.dcm.gz");
        try (OutputStream out = new GZIPOutputStream(new FileOutputStream(gzipped))) {
            Files.copy(fixture().toPath(), out);
        }

        final DicomObjectI dicomObject =
                DicomObjectFactory.newInstance(gzipped, DicomInputStream.IncludeBulkData.URI);

        final Object value = dicomObject.getAttributes().getValue(Tag.PixelData);
        assertTrue("pixel data should be a reference: "
                   + (value == null ? "null" : value.getClass().getName()),
                   value instanceof BulkData);
        final File spooled = ((BulkData) value).getFile();
        assertNotEquals("the reference should point at a spool file rather than at the gzipped source",
                        gzipped.getCanonicalFile(), spooled.getCanonicalFile());
        assertTrue("dcm4che should have spooled the bulk data to " + spooled, spooled.exists());

        final File written = write(dicomObject);
        try {
            assertFalse("the spooled file should be gone once the object has been written and released",
                        spooled.exists());
        } finally {
            Files.deleteIfExists(written.toPath());
        }
    }

    /**
     * The files dcm4che spools hold pixel data, so nobody but this user should be able to read them.
     * <p>
     * dcm4che creates them through the legacy {@code File.createTempFile}, which takes its mode from
     * the umask and typically leaves them rw-r--r--; on a shared host that is PHI any local account
     * can read. Their own mode is not ours to set, so the guard is the directory they sit in.
     * <p>
     * Both halves are asserted because neither is enough on its own. The permissions check is
     * vacuous wherever the shared temp directory already happens to be owner-only, as a macOS
     * per-user {@code TMPDIR} is; the check that the spool directory is not that shared directory
     * fails everywhere the wiring is missing.
     */
    @Test
    public void spoolsBulkDataWhereOnlyThisUserCanReadIt() throws Exception {
        final Path spoolRoot = temporaryFolder.getRoot().toPath();
        final FileStore store = Files.getFileStore(spoolRoot);
        assumeTrue("needs POSIX file permissions", store.supportsFileAttributeView("posix"));

        final File gzipped = temporaryFolder.newFile("permissions.dcm.gz");
        try (OutputStream out = new GZIPOutputStream(new FileOutputStream(gzipped))) {
            Files.copy(fixture().toPath(), out);
        }

        final DicomObjectI dicomObject =
                DicomObjectFactory.newInstance(gzipped, DicomInputStream.IncludeBulkData.URI);
        try {
            final File spooled = ((BulkData) dicomObject.getAttributes().getValue(Tag.PixelData)).getFile();
            assertNotEquals("bulk data should spool into a directory of ours, not straight into the "
                            + "shared temp directory, whose permissions are not ours to rely on",
                            new File(System.getProperty("java.io.tmpdir")).getCanonicalFile(),
                            spooled.getParentFile().getCanonicalFile());
            assertEquals("the directory dcm4che spooled into should be readable by nobody else: " + spooled,
                         PosixFilePermissions.fromString("rwx------"),
                         Files.getPosixFilePermissions(spooled.getParentFile().toPath()));
        } finally {
            dicomObject.releaseScratchFiles();
        }
    }

    /**
     * A gzipped source that ends partway through leaves dcm4che having spooled some of the bulk
     * data it read. Those files hold pixel data, and the constructor is throwing, so no object will
     * exist to release them later. They have to go before the exception leaves.
     */
    @Test
    public void deletesSpooledFilesWhenTheReadFails() throws Exception {
        final byte[] gzipped;
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            try (OutputStream out = new GZIPOutputStream(buffer)) {
                Files.copy(fixture().toPath(), out);
            }
            gzipped = buffer.toByteArray();
        }

        // Truncated hard enough to fail mid-read, but past the header and into the pixel data, so
        // there is something spooled by the time it does.
        final File truncated = temporaryFolder.newFile("truncated.dcm.gz");
        Files.write(truncated.toPath(), Arrays.copyOf(gzipped, (int) (gzipped.length * 0.9)));

        final File[] before = spoolFiles();
        try {
            DicomObjectFactory.newInstance(truncated, DicomInputStream.IncludeBulkData.URI);
            fail("expected a truncated source to fail the read");
        } catch (MizerException expected) {
            // The point is what it leaves behind, not the message.
        }
        final File[] after = spoolFiles();

        assertEquals("a failed read left " + (after.length - before.length) + " spool file(s) behind: "
                     + Arrays.toString(after), before.length, after.length);
    }

    /** dcm4che names its spool files blk*.tmp and puts them in the system temp directory. */
    private static File[] spoolFiles() {
        final File[] found = new File(System.getProperty("java.io.tmpdir"))
                .listFiles((directory, name) -> name.startsWith("blk") && name.endsWith(".tmp"));
        return found == null ? new File[0] : found;
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
