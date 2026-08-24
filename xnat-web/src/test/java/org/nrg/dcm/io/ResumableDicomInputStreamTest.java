package org.nrg.dcm.io;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import com.google.common.io.ByteStreams;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nrg.dicom.mizer.objects.Dcm4cheConvert;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

/**
 * Covers {@link ResumableDicomInputStream#openWithBulkDataOffHeap}, which exists for XNAT-7933: the importer's
 * read window is sized from the identifier's tags, so an identifier configured with a private tag in a group
 * &gt;= 0x8000 now reads past (7FE0,0010) where it never used to. These cover what that costs -- the pixel data
 * must not land on the heap, the bytes written must not change, and the files dcm4che spools instead must not
 * be left behind.
 */
public class ResumableDicomInputStreamTest {

    /** An ordinary identifier's tags top out at StudyComments (0032,4000). */
    private static final int ORDINARY_WINDOW = 0x00324001;
    /** A window sized from a configured (F215,1050), which sorts after the pixel data. */
    private static final int HIGH_GROUP_WINDOW = 0xF2151051;

    private static final String FIXTURE =
            Paths.get("src", "test", "resources", "dicom",
                      "1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm").toString();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @After
    public void clearScratchDirectory() {
        System.clearProperty(ResumableDicomInputStream.SCRATCH_DIR_PROPERTY);
    }

    @Test
    public void keepsPixelDataOffTheHeapWhenTheWindowReachesIt() throws Exception {
        final List<File> spooled = new ArrayList<>();
        final Attributes dataset = read(HIGH_GROUP_WINDOW, spooled);

        final Object pixelData = dataset.getValue(Tag.PixelData);
        assertTrue("the window should have reached the pixel data", dataset.contains(Tag.PixelData));
        assertTrue("pixel data should be a reference, not a heap byte[], but was "
                   + (pixelData == null ? "null" : pixelData.getClass().getName()),
                   pixelData instanceof BulkData);
        assertEquals("dcm4che should report the file it spooled to", 1, spooled.size());

        ResumableDicomInputStream.deleteBulkDataFiles(spooled);
    }

    /**
     * The window is unchanged for an ordinary identifier, so an ordinary import must behave exactly as it did:
     * the read stops short of the pixel data and nothing is spooled.
     */
    @Test
    public void spoolsNothingForAnOrdinaryWindow() throws Exception {
        final List<File> spooled = new ArrayList<>();
        final Attributes dataset = read(ORDINARY_WINDOW, spooled);

        assertFalse("an ordinary window stops before the pixel data", dataset.contains(Tag.PixelData));
        assertTrue("nothing should be spooled when no bulk data is read", spooled.isEmpty());
    }

    /**
     * The importer writes the dataset it read and then copies the rest of the stream, so keeping the pixel data
     * out of the dataset must not change a byte of what lands in the archive.
     */
    @Test
    public void writesTheSameBytesAsAnOnHeapRead() throws Exception {
        final List<File> spooled = new ArrayList<>();
        final File offHeap = reassemble(true, spooled, "off-heap.dcm");
        final File onHeap = reassemble(false, new ArrayList<>(), "on-heap.dcm");

        assertArrayEquals("a reference-backed write must produce the same file as an on-heap one",
                          Files.readAllBytes(onHeap.toPath()), Files.readAllBytes(offHeap.toPath()));

        final byte[] original = (byte[]) readWhole(new File(FIXTURE)).getValue(Tag.PixelData);
        final byte[] written = (byte[]) readWhole(offHeap).getValue(Tag.PixelData);
        assertArrayEquals("the pixel data must survive the round trip", original, written);

        ResumableDicomInputStream.deleteBulkDataFiles(spooled);
    }

    /** dcm4che does not clean these up, so the importer has to. */
    @Test
    public void deleteBulkDataFilesRemovesEverythingItSpooled() throws Exception {
        final List<File> spooled = new ArrayList<>();
        read(HIGH_GROUP_WINDOW, spooled);
        assertFalse("the read should have spooled something to delete", spooled.isEmpty());
        for (final File file : spooled) {
            assertTrue("spool file should exist before cleanup: " + file, file.exists());
        }

        ResumableDicomInputStream.deleteBulkDataFiles(spooled);

        for (final File file : spooled) {
            assertFalse("spool file should be gone after cleanup: " + file, file.exists());
        }
    }

    /**
     * java.io.tmpdir may be smaller than an image, and need not carry the archive's access controls, so where
     * the pixel data is staged has to be something a site can decide.
     */
    @Test
    public void spoolsIntoTheConfiguredDirectory() throws Exception {
        final File scratch = folder.newFolder("scratch");
        System.setProperty(ResumableDicomInputStream.SCRATCH_DIR_PROPERTY, scratch.getAbsolutePath());

        final List<File> spooled = new ArrayList<>();
        read(HIGH_GROUP_WINDOW, spooled);

        assertEquals("the read should have spooled once", 1, spooled.size());
        assertEquals("the spool file should be in the configured directory",
                     scratch.getCanonicalFile(), spooled.get(0).getParentFile().getCanonicalFile());

        ResumableDicomInputStream.deleteBulkDataFiles(spooled);
    }

    /** A site setting this before the directory exists should not have to create it by hand. */
    @Test
    public void createsTheConfiguredDirectoryWhenItIsMissing() throws Exception {
        final File scratch = new File(folder.getRoot(), "not-yet");
        assertFalse("precondition: the directory should not exist", scratch.exists());
        System.setProperty(ResumableDicomInputStream.SCRATCH_DIR_PROPERTY, scratch.getAbsolutePath());

        final List<File> spooled = new ArrayList<>();
        read(HIGH_GROUP_WINDOW, spooled);

        assertTrue("the directory should have been created", scratch.isDirectory());
        assertEquals(scratch.getCanonicalFile(), spooled.get(0).getParentFile().getCanonicalFile());

        ResumableDicomInputStream.deleteBulkDataFiles(spooled);
    }

    /**
     * Falling back to java.io.tmpdir would defeat the reason for configuring a directory, which may be that
     * the pixel data must not go there.
     */
    @Test
    public void failsRatherThanFallingBackWhenTheDirectoryCannotBeCreated() throws Exception {
        final File blocker = folder.newFile("a-file-not-a-directory");
        System.setProperty(ResumableDicomInputStream.SCRATCH_DIR_PROPERTY,
                           new File(blocker, "scratch").getAbsolutePath());

        try (final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(FIXTURE))) {
            ResumableDicomInputStream.openWithBulkDataOffHeap(bis);
            fail("expected an IOException naming the directory it could not create");
        } catch (IOException expected) {
            assertTrue("the message should name the directory, but was: " + expected.getMessage(),
                       expected.getMessage().contains("scratch"));
        }
    }

    /** Reads the fixture through the importer's own stream configuration, up to stopTag. */
    private Attributes read(final int stopTag, final List<File> spooled) throws IOException {
        try (final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(FIXTURE));
             final ResumableDicomInputStream dis = ResumableDicomInputStream.openWithBulkDataOffHeap(bis)) {
            dis.readFileMetaInformation();
            final Attributes dataset = new Attributes();
            dis.readAttributes(dataset, -1, stopTag);
            spooled.addAll(dis.getBulkDataFiles());
            return dataset;
        }
    }

    /**
     * Runs the importer's contract end to end -- read a prefix, rewind, write that prefix, append the rest of
     * the stream -- with bulk data either referenced or on the heap.
     */
    private File reassemble(final boolean offHeap, final List<File> spooled, final String name) throws Exception {
        final File output = folder.newFile(name);
        try (final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(FIXTURE));
             final DicomInputStream dis = offHeap
                                          ? ResumableDicomInputStream.openWithBulkDataOffHeap(bis)
                                          : new ResumableDicomInputStream(bis)) {
            Attributes fmi = dis.readFileMetaInformation();
            final String transferSyntaxUID = dis.getTransferSyntax();
            final Attributes dataset = new Attributes();
            dis.readAttributes(dataset, -1, HIGH_GROUP_WINDOW);
            spooled.addAll(dis.getBulkDataFiles());
            dis.reset();

            if (null == fmi || !fmi.contains(Tag.TransferSyntaxUID)) {
                fmi = dataset.createFileMetaInformation(transferSyntaxUID);
            }
            dataset.addAll(fmi);

            // Mirrors GradualDicomImporter.write, which cannot be called from here: any static touch of that
            // class runs ImporterHandlerA's initializer, which needs a Spring context.
            final Dcm4cheConvert.SplitAttributes split = Dcm4cheConvert.splitFmiAndDataset(dataset);
            try (final FileOutputStream fos = new FileOutputStream(output);
                 final BufferedOutputStream bos = new BufferedOutputStream(fos);
                 final DicomOutputStream dos = new DicomOutputStream(bos, UID.ExplicitVRLittleEndian)) {
                dos.writeDataset(split.fmi, split.onlyDataset);
                dos.flush();
                ByteStreams.copy(bis, bos);
            }
        }
        return output;
    }

    private static Attributes readWhole(final File file) throws IOException {
        try (final DicomInputStream dis = new DicomInputStream(file)) {
            return dis.readDataset(-1, -1);
        }
    }
}
