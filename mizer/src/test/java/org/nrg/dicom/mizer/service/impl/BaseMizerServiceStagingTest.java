package org.nrg.dicom.mizer.service.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.AnonymizationResultSuccess;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.impl.test.TestMizer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * In-place anonymization stages its output where the {@link org.nrg.dicom.mizer.service.StagingDirectoryResolver}
 * says, and the file that ends up at the original path is the anonymized object with its pixel data intact.
 */
public class BaseMizerServiceStagingTest {

    private static final String FIXTURE = "dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm";
    private static final String SCRIPT  = "version \"6.1\"\n(0010,0010) := subject";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void stagesWhereTheResolverSaysAndReplacesTheSourceInPlace() throws Exception {
        final File source = temporaryFolder.newFile("1.dcm");
        Files.copy(fixture().toPath(), source.toPath(), StandardCopyOption.REPLACE_EXISTING);
        final Attributes before       = read(source);
        final byte[]     pixelsBefore = pixelDigest(source);

        final File                  staging  = temporaryFolder.newFolder("staging");
        final AtomicReference<File> askedFor = new AtomicReference<>();
        final BaseMizerService      service  = new BaseMizerService(Collections.singletonList(new TestMizer()));
        service.setStagingDirectoryResolver(dicomFile -> {
            askedFor.set(dicomFile);
            return staging;
        });

        final AnonymizationResult result = service.anonymize(source, "project", "subject", "session", true, false, 7L, SCRIPT);

        assertTrue("the anonymization should have succeeded: " + result.getMessage(), result instanceof AnonymizationResultSuccess);
        assertEquals("the resolver should be asked about the file being anonymized", source, askedFor.get());
        assertTrue("the anonymized object should be at the original path", source.isFile());
        assertEquals("the staging directory should be empty once the file is in place", 0, staging.list().length);

        final Attributes after = read(source);
        assertEquals("the object at the original path should be the same instance", before.getString(Tag.SOPInstanceUID), after.getString(Tag.SOPInstanceUID));
        assertTrue("the anonymization should have been recorded in the header", after.contains(Tag.DeidentificationMethodCodeSequence));
        assertArrayEquals("pixel data must survive the replacement byte for byte", pixelsBefore, pixelDigest(source));
    }

    /**
     * A script that fails on an object reports that as an error result, not an exception. The
     * object must then be exactly what it was: the staging file used to be opened before the
     * script ran, so a failure left it empty, and the empty file was put in the object's place.
     */
    @Test
    public void leavesTheObjectUntouchedWhenTheScriptFails() throws Exception {
        final File source = temporaryFolder.newFile("1.dcm");
        Files.copy(fixture().toPath(), source.toPath(), StandardCopyOption.REPLACE_EXISTING);
        final byte[] before  = Files.readAllBytes(source.toPath());
        final File   staging = temporaryFolder.newFolder("staging");

        final BaseMizerService service = new BaseMizerService(Collections.singletonList(new TestMizer() {
            @Override
            protected AnonymizationResult anonymizeImpl(final DicomObjectI dicomObject, final MizerContextWithScript context) {
                return new AnonymizationResultError(dicomObject, "no codec for the transfer syntax");
            }
        }));
        service.setStagingDirectoryResolver(dicomFile -> staging);

        final AnonymizationResult result = service.anonymize(source, "project", "subject", "session", true, false, 7L, SCRIPT);

        assertTrue("the failure should come back as an error result: " + result.getMessage(), result instanceof AnonymizationResultError);
        assertArrayEquals("the object must be exactly what it was", before, Files.readAllBytes(source.toPath()));
        assertEquals("no staged file may be left behind", 0, staging.list().length);
    }

    /**
     * A batch is anonymized all or nothing. Regression: each file was put in place as soon as it was
     * anonymized, so a failure on a later file left the earlier ones changed, and archiving again
     * after fixing the script ran it over them a second time.
     */
    @Test
    public void leavesEveryFileInTheBatchUntouchedWhenOneFails() throws Exception {
        final File first  = copyOfFixture("1.dcm");
        final File second = copyOfFixture("2.dcm");
        final byte[] firstBefore  = Files.readAllBytes(first.toPath());
        final byte[] secondBefore = Files.readAllBytes(second.toPath());
        final File   staging      = temporaryFolder.newFolder("staging");

        final AtomicInteger calls = new AtomicInteger();
        final BaseMizerService service = new BaseMizerService(Collections.singletonList(new TestMizer() {
            @Override
            protected AnonymizationResult anonymizeImpl(final DicomObjectI dicomObject, final MizerContextWithScript context) throws MizerException {
                return calls.incrementAndGet() == 2
                       ? new AnonymizationResultError(dicomObject, "no codec for the transfer syntax")
                       : super.anonymizeImpl(dicomObject, context);
            }
        }));
        service.setStagingDirectoryResolver(dicomFile -> staging);

        assertThrows(MizerException.class,
                     () -> service.anonymize(Arrays.asList(first, second), "project", "subject", "session", 7L, SCRIPT, true, false));

        assertEquals("both files should have been tried", 2, calls.get());
        assertArrayEquals("the file anonymized before the failure must be exactly what it was", firstBefore, Files.readAllBytes(first.toPath()));
        assertArrayEquals("the file that failed must be exactly what it was", secondBefore, Files.readAllBytes(second.toPath()));
        assertEquals("no staged file may be left behind", 0, staging.list().length);
    }

    @Test
    public void putsTheWholeBatchInPlaceWhenEveryFileSucceeds() throws Exception {
        final File first   = copyOfFixture("1.dcm");
        final File second  = copyOfFixture("2.dcm");
        final File staging = temporaryFolder.newFolder("staging");
        final BaseMizerService service = new BaseMizerService(Collections.singletonList(new TestMizer()));
        service.setStagingDirectoryResolver(dicomFile -> staging);

        final List<AnonymizationResult> results = service.anonymize(Arrays.asList(first, second), "project", "subject", "session", 7L, SCRIPT, true, false);

        assertEquals(2, results.size());
        for (final File file : Arrays.asList(first, second)) {
            assertTrue(file + " should have been anonymized in place", read(file).contains(Tag.DeidentificationMethodCodeSequence));
        }
        assertEquals("the staging directory should be empty once the batch is in place", 0, staging.list().length);
    }

    private File copyOfFixture(final String name) throws Exception {
        final File copy = temporaryFolder.newFile(name);
        Files.copy(fixture().toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return copy;
    }

    private static File fixture() throws Exception {
        return new File(BaseMizerServiceStagingTest.class.getClassLoader().getResource(FIXTURE).toURI());
    }

    private static Attributes read(final File file) throws Exception {
        try (DicomInputStream in = new DicomInputStream(file)) {
            return in.readDataset();
        }
    }

    private static byte[] pixelDigest(final File file) throws Exception {
        final byte[] pixels = read(file).getBytes(Tag.PixelData);
        assertTrue("the fixture must carry pixel data for this test to mean anything", pixels != null && pixels.length > 0);
        return MessageDigest.getInstance("SHA-256").digest(pixels);
    }
}
