package org.nrg.xnat.archive;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.imageio.codec.Transcoder;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nrg.dcm.io.ResumableDicomInputStream;
import org.nrg.dicom.dicomedit.mizer.DE6Mizer;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultReject;
import org.nrg.dicom.mizer.objects.AnonymizationResultSuccess;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.dicom.mizer.service.impl.BaseMizerService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNoException;

/**
 * The importer historically wrote a received object and then anonymized the file. Reading the whole
 * object, running the scripts in memory and writing once has to leave exactly the same bytes in the
 * archive, so every case here runs both ways and compares the files.
 */
public class ReceivedDicomObjectTest {

    private static final Path RESOURCES = Paths.get("src", "test", "resources");
    private static final File MR_FIXTURE = RESOURCES.resolve("dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm").toFile();
    private static final File TAIL_FIXTURE = RESOURCES.resolve("dicomHeaderDump_tagAfterPixelData.dcm").toFile();

    /** The window the importer reads for an ordinary identifier. */
    private static final int ORDINARY_LAST_TAG = Tag.SeriesDescription;
    private static final String AE_TITLE = "SENDER";

    private static final String SITE_SCRIPT = String.join("\n",
            "version \"6.1\"",
            "project != \"Unassigned\" ? (0008,1030) := project",
            "(0010,0010) := subject",
            "(0010,0020) := session",
            "- (0010,0030)");
    private static final String REDACTING_SCRIPT = String.join("\n",
            "version \"6.1\"",
            "(0010,0010) := subject",
            "alterPixels[\"rectangle\", \"l=10, t=10, r=60, b=60\", \"solid\", \"v=0\"]");
    private static final String REJECTING_SCRIPT = String.join("\n",
            "version \"6.1\"",
            "reject[]");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private BaseMizerService mizer;
    private File scratch;

    @Before
    public void setUp() throws Exception {
        mizer   = new BaseMizerService(new ArrayList<>(Collections.singletonList(new DE6Mizer())));
        scratch = folder.newFolder("scratch");
        System.setProperty(ResumableDicomInputStream.SCRATCH_DIR_PROPERTY, scratch.getAbsolutePath());
    }

    @After
    public void tearDown() {
        System.clearProperty(ResumableDicomInputStream.SCRATCH_DIR_PROPERTY);
    }

    @Test
    public void wholeReadKeepsPixelDataOffTheHeap() throws Exception {
        try (ReceivedDicomObject received = ReceivedDicomObject.read(open(MR_FIXTURE), null, ORDINARY_LAST_TAG, true)) {
            final Object pixelData = received.getDataset().getValue(Tag.PixelData);
            assertTrue("pixel data should be a reference, not a heap byte[], but was "
                       + (pixelData == null ? "null" : pixelData.getClass().getName()), pixelData instanceof BulkData);
            assertTrue(received.isWhole());
            assertFalse("the spool should hold the pixel data while the object is open", filesUnder(scratch).isEmpty());
        }
        assertTrue("closing should delete everything spooled", filesUnder(scratch).isEmpty());
    }

    @Test
    public void partialReadStopsBeforeThePixelDataAndSpoolsNothing() throws Exception {
        try (ReceivedDicomObject received = ReceivedDicomObject.read(open(MR_FIXTURE), null, ORDINARY_LAST_TAG, false)) {
            assertFalse(received.isWhole());
            assertFalse(received.getDataset().contains(Tag.PixelData));
            assertTrue("the FMI should be merged into the dataset", received.getDataset().contains(Tag.TransferSyntaxUID));
            assertTrue(filesUnder(scratch).isEmpty());
        }
    }

    /**
     * With no script there is no second pass either way, so the two reads must write the same object. Compared
     * as parsed rather than as bytes: a partial read copies the tail of the stream through as it arrived, while
     * a whole read re-serializes it, and dcm4che normalizes sequence encodings when it does. That difference is
     * only reachable when a script runs, and then the old path re-serialized too, which
     * {@link #scriptInMemoryLeavesTheSameFileAsScriptOnTheWrittenFile} pins byte for byte.
     */
    @Test
    public void wholeAndPartialReadsWriteTheSameObjectWhenNoScriptRuns() throws Exception {
        for (final File fixture : fixtures()) {
            final File fromPartial = write(fixture, null, false, "partial-" + fixture.getName());
            final File fromWhole   = write(fixture, null, true, "whole-" + fixture.getName());
            assertEquals(fixture.getName(), readWhole(fromPartial), readWhole(fromWhole));
            assertArrayEquals(fixture.getName() + ": pixel data", pixelData(fromPartial), pixelData(fromWhole));
        }
    }

    /** The bytes the archive ends up with must not depend on whether the script ran in memory or on the file. */
    @Test
    public void scriptInMemoryLeavesTheSameFileAsScriptOnTheWrittenFile() throws Exception {
        for (final File fixture : fixtures()) {
            assertSameResultEitherWay(fixture, SITE_SCRIPT);
        }
    }

    @Test
    public void redactionInMemoryLeavesTheSameFileAsRedactionOnTheWrittenFile() throws Exception {
        assertSameResultEitherWay(MR_FIXTURE, REDACTING_SCRIPT);
    }

    /** Compressed pixels go through the codec both ways; skipped where the native codec is not installed. */
    @Test
    public void redactionOfCompressedPixelsInMemoryLeavesTheSameFile() throws Exception {
        final File compressed = folder.newFile("compressed.dcm");
        try (Transcoder transcoder = new Transcoder(MR_FIXTURE)) {
            transcoder.setIncludeFileMetaInformation(true);
            transcoder.setDestinationTransferSyntax(UID.JPEG2000Lossless);
            transcoder.transcode((t, dataset) -> new FileOutputStream(compressed));
        } catch (Throwable e) {
            assumeNoException("JPEG 2000 codec is not usable in this environment", e);
        }
        assertSameResultEitherWay(compressed, REDACTING_SCRIPT);
    }

    /**
     * An element after the pixel data must survive a whole read and come back out. Tags are unsigned, and dcm4che
     * orders them as signed ints, so the element that sorts last in the file is found with an unsigned maximum.
     */
    @Test
    public void wholeReadKeepsElementsAfterThePixelData() throws Exception {
        final Attributes original = readWhole(TAIL_FIXTURE);
        final int lastTag = Arrays.stream(original.tags()).boxed().max(Integer::compareUnsigned).orElseThrow();
        assertTrue("precondition: the fixture should carry an element after the pixel data",
                   Integer.compareUnsigned(lastTag, Tag.PixelData) > 0);

        final File written = write(TAIL_FIXTURE, null, true, "tail.dcm");
        final Attributes reread = readWhole(written);
        assertTrue(reread.contains(lastTag));
        assertArrayEquals(original.getBytes(lastTag), reread.getBytes(lastTag));
    }

    /** A C-STORE carries no file meta information; the caller supplies the negotiated transfer syntax. */
    @Test
    public void createsFileMetaInformationWhenTheStreamHasNone() throws Exception {
        final File bare = folder.newFile("bare.dcm");
        try (DicomInputStream in = new DicomInputStream(MR_FIXTURE);
             DicomOutputStream out = new DicomOutputStream(new FileOutputStream(bare), UID.ExplicitVRLittleEndian)) {
            in.readFileMetaInformation();
            out.writeDataset(null, in.readDataset());
        }

        final File fromPartial = write(bare, UID.ExplicitVRLittleEndian, false, "bare-partial.dcm");
        final File fromWhole   = write(bare, UID.ExplicitVRLittleEndian, true, "bare-whole.dcm");
        for (final File file : Arrays.asList(fromPartial, fromWhole)) {
            try (DicomInputStream in = new DicomInputStream(file)) {
                final Attributes fmi = in.readFileMetaInformation();
                assertEquals(UID.ExplicitVRLittleEndian, fmi.getString(Tag.TransferSyntaxUID));
                assertEquals(AE_TITLE, fmi.getString(Tag.SourceApplicationEntityTitle));
            }
        }
        assertArrayEquals(Files.readAllBytes(fromPartial.toPath()), Files.readAllBytes(fromWhole.toPath()));
    }

    /**
     * A Deflated source is one continuous zlib stream past the file meta group, so the partial read's
     * copy-the-remainder trick cannot reassemble it. It must be read whole even when no script asks for
     * one, and the object it writes must re-read cleanly. Regression: the partial path wrote a fresh
     * header followed by the raw compressed remainder, which came back malformed and failed re-read.
     */
    @Test
    public void deflatedIsAlwaysReadWholeAndRoundTrips() throws Exception {
        final File deflated = deflate(MR_FIXTURE);
        // Ask for a partial read, as an import with no script would; the Deflated source overrides it.
        try (ReceivedDicomObject received = ReceivedDicomObject.read(open(deflated), null, ORDINARY_LAST_TAG, false)) {
            assertEquals(UID.DeflatedExplicitVRLittleEndian, received.getTransferSyntax());
            assertTrue("a Deflated source must be read whole regardless of the caller's request", received.isWhole());
        }

        final File written = write(deflated, null, false, "deflated-out.dcm");
        try (DicomInputStream in = new DicomInputStream(written)) {
            assertEquals("the written object must still be Deflated",
                         UID.DeflatedExplicitVRLittleEndian, in.readFileMetaInformation().getString(Tag.TransferSyntaxUID));
        }
        assertArrayEquals("pixels must survive the deflate round-trip", pixelData(deflated), pixelData(written));
        assertTrue("nothing should be left in the spool", filesUnder(scratch).isEmpty());
    }

    @Test
    public void rejectionInMemoryWritesNothing() throws Exception {
        final File output = new File(folder.getRoot(), "rejected.dcm");
        try (ReceivedDicomObject received = ReceivedDicomObject.read(open(MR_FIXTURE), null, ORDINARY_LAST_TAG, true)) {
            final AnonymizationResult result = mizer.anonymize(wrap(received.getDataset()), context(REJECTING_SCRIPT));
            assertTrue(result instanceof AnonymizationResultReject);
            // The importer returns without writing on a rejection; nothing else needs to happen here.
        }
        assertFalse(output.exists());
        assertTrue(filesUnder(scratch).isEmpty());
    }

    /**
     * A file-backed source -- the inbox hands the importer exactly these -- needs no spool: pixel
     * data is referenced straight into the source file, which must come through the read, the write
     * and the close untouched.
     */
    @Test
    public void fileBackedWholeReadReferencesPixelsIntoTheSourceWithoutSpooling() throws Exception {
        final File source = folder.newFile("inbox.dcm");
        Files.copy(MR_FIXTURE.toPath(), source.toPath(), StandardCopyOption.REPLACE_EXISTING);
        final byte[] sourceBytes = Files.readAllBytes(source.toPath());

        final File output = new File(folder.getRoot(), "inbox-out.dcm");
        try (ReceivedDicomObject received = ReceivedDicomObject.read(open(source), null, ORDINARY_LAST_TAG, true, source)) {
            final Object pixelData = received.getDataset().getValue(Tag.PixelData);
            assertTrue("pixel data should be a reference, not a heap byte[]", pixelData instanceof BulkData);
            assertTrue("the reference should point into the source file, not at a spool copy",
                       ((BulkData) pixelData).getURI().startsWith(source.toURI().toString()));
            assertTrue("nothing should be spooled for a file-backed read", filesUnder(scratch).isEmpty());
            received.write(received.getDataset(), AE_TITLE, output, "test");
        }
        assertArrayEquals("the source must come through the read, write and close untouched",
                          sourceBytes, Files.readAllBytes(source.toPath()));
        assertArrayEquals("the written object must carry the source's pixels",
                          pixelData(source), pixelData(output));
    }

    /**
     * A Deflated file's stream positions are inflated offsets, not file offsets, so a file-backed
     * Deflated read must ignore the file and spool as the stream case does.
     */
    @Test
    public void fileBackedDeflatedReadStillSpoolsAndRoundTrips() throws Exception {
        final File   deflated    = deflate(MR_FIXTURE);
        final byte[] sourceBytes = Files.readAllBytes(deflated.toPath());

        final File output = new File(folder.getRoot(), "deflated-filebacked-out.dcm");
        try (ReceivedDicomObject received = ReceivedDicomObject.read(open(deflated), null, ORDINARY_LAST_TAG, true, deflated)) {
            final Object pixelData = received.getDataset().getValue(Tag.PixelData);
            assertTrue("pixel data should be a reference, not a heap byte[]", pixelData instanceof BulkData);
            assertFalse("a Deflated file's offsets are not file offsets, so the reference must not point into it",
                        ((BulkData) pixelData).getURI().startsWith(deflated.toURI().toString()));
            received.write(received.getDataset(), AE_TITLE, output, "test");
        }
        assertArrayEquals("the source must be untouched", sourceBytes, Files.readAllBytes(deflated.toPath()));
        assertArrayEquals("pixels must survive the spooled read", pixelData(MR_FIXTURE), pixelData(output));
        assertTrue("the spool must be cleaned up on close", filesUnder(scratch).isEmpty());
    }

    /**
     * dcm4che inflates three transfer syntaxes, not one. Each must force the whole read: a partial read of
     * any of them would copy leftover compressed bytes after a fresh header and write a malformed object.
     */
    @Test
    public void everyInflatedSyntaxIsReadWholeAndRoundTrips() throws Exception {
        for (final String transferSyntax : new String[] {
                UID.DeflatedExplicitVRLittleEndian, UID.JPIPReferencedDeflate, UID.JPIPHTJ2KReferencedDeflate}) {
            final File deflated = deflate(MR_FIXTURE, transferSyntax);
            final File output   = new File(folder.getRoot(), "whole-" + transferSyntax + ".dcm");
            try (ReceivedDicomObject received = ReceivedDicomObject.read(open(deflated), null, ORDINARY_LAST_TAG, false)) {
                assertTrue(transferSyntax + " must force the whole read even when a partial one was asked for", received.isWhole());
                received.write(received.getDataset(), AE_TITLE, output, "test");
            }
            assertArrayEquals(transferSyntax + ": pixels must survive the round trip", pixelData(MR_FIXTURE), pixelData(output));
        }
    }

    /**
     * A C-STORE carries the dataset alone, deflated from its first byte, and names its syntax only in
     * the association. With no file meta group to learn it from, the read has to take the syntax the
     * caller negotiated. Regression: the stream guessed from the compressed bytes and the object
     * failed to read.
     */
    @Test
    public void negotiatedDeflatedDatasetWithoutFileMetaIsInflatedAndRoundTrips() throws Exception {
        for (final String transferSyntax : new String[] {
                UID.DeflatedExplicitVRLittleEndian, UID.JPIPReferencedDeflate, UID.JPIPHTJ2KReferencedDeflate}) {
            final File bare   = deflatedDatasetOnly(MR_FIXTURE, transferSyntax);
            final File output = new File(folder.getRoot(), "negotiated-" + transferSyntax + ".dcm");
            try (ReceivedDicomObject received = ReceivedDicomObject.read(open(bare), transferSyntax, ORDINARY_LAST_TAG, false)) {
                assertTrue(transferSyntax + " must force the whole read even when a partial one was asked for", received.isWhole());
                received.write(received.getDataset(), AE_TITLE, output, "test");
            }
            try (DicomInputStream in = new DicomInputStream(output)) {
                assertEquals(transferSyntax + ": the written object must keep the negotiated syntax",
                             transferSyntax, in.readFileMetaInformation().getString(Tag.TransferSyntaxUID));
            }
            assertArrayEquals(transferSyntax + ": pixels must survive the round trip", pixelData(MR_FIXTURE), pixelData(output));
        }
    }

    /** A caller naming the syntax of a stream that carries its own file meta group must not make the read skip that group. */
    @Test
    public void deflatedFileStillReadsWhenTheCallerAlsoNamesItsSyntax() throws Exception {
        final File deflated = deflate(MR_FIXTURE);
        final File written  = write(deflated, UID.DeflatedExplicitVRLittleEndian, false, "named-deflated-out.dcm");
        assertArrayEquals("pixels must survive the deflate round-trip", pixelData(deflated), pixelData(written));
    }

    /** What an inbox holding a README or a zero-byte file hands the importer: the stream never opens, and the source must still be closed. */
    @Test
    public void closesTheSourceWhenItCannotBeReadAsDicom() {
        final AtomicBoolean closed = new AtomicBoolean();
        final InputStream empty = new ByteArrayInputStream(new byte[0]) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        };
        assertThrows(IOException.class, () -> ReceivedDicomObject.read(empty, null, ORDINARY_LAST_TAG, false).close());
        assertTrue("the source must be closed even though the stream never opened", closed.get());
    }

    /**
     * Old way: partial read, write, anonymize the file in place. New way: whole read, anonymize in memory,
     * write. Same script, same variables, same recorded script id.
     */
    private void assertSameResultEitherWay(final File fixture, final String script) throws Exception {
        final File onFile = write(fixture, null, false, "on-file-" + fixture.getName());
        final AnonymizationResult fileResult = mizer.anonymize(onFile, context(script));
        assertTrue(fixture.getName() + ": " + fileResult.getMessage(), fileResult instanceof AnonymizationResultSuccess);

        final File inMemory = new File(folder.getRoot(), "in-memory-" + fixture.getName());
        final List<DicomObjectI> wrappers = new ArrayList<>();
        try (ReceivedDicomObject received = ReceivedDicomObject.read(open(fixture), null, ORDINARY_LAST_TAG, true)) {
            final DicomObjectI wrapper = wrap(received.getDataset());
            wrappers.add(wrapper);
            final AnonymizationResult memoryResult = mizer.anonymize(wrapper, context(script));
            assertTrue(fixture.getName() + ": " + memoryResult.getMessage(), memoryResult instanceof AnonymizationResultSuccess);
            received.write(received.getDataset(), AE_TITLE, inMemory, "test");
        } finally {
            wrappers.forEach(DicomObjectI::releaseScratchFiles);
        }

        assertArrayEquals(fixture.getName() + ": the archive must hold the same bytes whichever way the script ran",
                          Files.readAllBytes(onFile.toPath()), Files.readAllBytes(inMemory.toPath()));
        assertTrue("nothing should be left in the spool", filesUnder(scratch).isEmpty());
    }

    private File write(final File fixture, final String transferSyntax, final boolean whole, final String name) throws IOException {
        final File output = new File(folder.getRoot(), name);
        try (ReceivedDicomObject received = ReceivedDicomObject.read(open(fixture), transferSyntax, ORDINARY_LAST_TAG, whole)) {
            received.write(received.getDataset(), AE_TITLE, output, "test");
        }
        return output;
    }

    /** Re-encode a fixture as Deflated Explicit VR Little Endian, the way write() does: FMI carries the TS and the dataset is deflated after it. */
    private File deflate(final File source) throws IOException {
        return deflate(source, UID.DeflatedExplicitVRLittleEndian);
    }

    /** Re-encode a fixture in any of the deflating syntaxes: FMI carries the TS and the dataset is deflated after it. */
    private File deflate(final File source, final String transferSyntax) throws IOException {
        final File deflated = folder.newFile("deflated-" + transferSyntax + ".dcm");
        final Attributes fmi;
        final Attributes dataset;
        try (DicomInputStream in = new DicomInputStream(source)) {
            in.readFileMetaInformation();
            dataset = in.readDataset();
            fmi = dataset.createFileMetaInformation(transferSyntax);
        }
        try (DicomOutputStream out = new DicomOutputStream(new FileOutputStream(deflated), UID.ExplicitVRLittleEndian)) {
            out.writeDataset(fmi, dataset);
        }
        return deflated;
    }

    /** The dataset alone, deflated in <b>transferSyntax</b> from its first byte, as a C-STORE carries it: no preamble, no file meta group. */
    private File deflatedDatasetOnly(final File source, final String transferSyntax) throws IOException {
        final File bare = folder.newFile("bare-" + transferSyntax + ".dcm");
        try (DicomInputStream in = new DicomInputStream(source);
             DicomOutputStream out = new DicomOutputStream(new FileOutputStream(bare), transferSyntax)) {
            in.readFileMetaInformation();
            out.writeDataset(null, in.readDataset());
        }
        return bare;
    }

    private MizerContext context(final String script) throws Exception {
        return mizer.createContext("PROJ", "SUBJ", "SESS", 7L, script, true, false);
    }

    private static DicomObjectI wrap(final Attributes dataset) {
        return new DicomObjectFactory.MizerDicomObject(dataset);
    }

    private static InputStream open(final File file) throws IOException {
        return new FileInputStream(file);
    }

    private static Attributes readWhole(final File file) throws IOException {
        try (DicomInputStream in = new DicomInputStream(file)) {
            return in.readDataset();
        }
    }

    private static byte[] pixelData(final File file) throws IOException {
        final byte[] pixels = readWhole(file).getBytes(Tag.PixelData);
        return pixels == null ? new byte[0] : pixels;
    }

    private static List<File> fixtures() throws IOException {
        try (Stream<Path> files = Files.list(RESOURCES.resolve("dicom"))) {
            final List<File> fixtures = files.filter(path -> path.toString().endsWith(".dcm"))
                                             .sorted()
                                             .map(Path::toFile)
                                             .collect(Collectors.toList());
            fixtures.add(TAIL_FIXTURE);
            fixtures.add(RESOURCES.resolve("contrastParser_plain.dcm").toFile());
            fixtures.add(RESOURCES.resolve("contrastParser_enhanced.dcm").toFile());
            return fixtures;
        }
    }

    private static List<Path> filesUnder(final File directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            return paths.filter(Files::isRegularFile).collect(Collectors.toList());
        }
    }
}
