package org.nrg.xnat.restlet.actions;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultNoOp;
import org.nrg.dicom.mizer.objects.AnonymizationResultReject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * A session import's site script runs after the upload is unpacked into the prearchive. When it fails, the session
 * must not stay there with its instances as they arrived.
 */
public class SessionImportSiteAnonymizationTest {
    private static final MizerException SCRIPT_FAILURE = new MizerException("Unable to anonymize 1.dcm");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final AtomicInteger removals = new AtomicInteger();

    @Test
    public void aSucceedingScriptLeavesTheSession() throws Exception {
        final List<AnonymizationResult> results = Collections.singletonList(new AnonymizationResultNoOp(null, "nothing to do"));
        assertSame(results, SessionImportSiteAnonymization.run(() -> results, this::remove));
        assertEquals("the session should not be removed", 0, removals.get());
    }

    @Test
    public void aFailingScriptRemovesTheSession() {
        final SessionImportSiteAnonymization.SessionRemovedException removed =
                assertThrows(SessionImportSiteAnonymization.SessionRemovedException.class,
                             () -> SessionImportSiteAnonymization.run(this::fail, this::remove));
        assertEquals("the session should be removed once", 1, removals.get());
        assertSame(SCRIPT_FAILURE, removed.getCause());
    }

    @Test
    public void aSessionAlreadyGoneLeavesTheScriptFailure() {
        final Exception thrown = assertThrows(Exception.class, () -> SessionImportSiteAnonymization.run(this::fail, () -> false));
        assertSame(SCRIPT_FAILURE, thrown);
    }

    @Test
    public void aFailedRemovalKeepsTheScriptFailure() {
        final Exception removalFailure = new IllegalStateException("prearchive unavailable");
        final MizerException scriptFailure = new MizerException("Unable to anonymize 2.dcm");
        final Exception thrown = assertThrows(Exception.class, () -> SessionImportSiteAnonymization.run(() -> {
            throw scriptFailure;
        }, () -> {
            throw removalFailure;
        }));
        assertSame(scriptFailure, thrown);
        assertArrayEquals(new Throwable[] {removalFailure}, thrown.getSuppressed());
    }

    @Test
    public void aSessionTheImportCreatedIsRemovedWhole() throws Exception {
        final SessionImportSiteAnonymization.SessionFiles before = SessionImportSiteAnonymization.SessionFiles.of(new File(folder.getRoot(), "not-yet-created"));
        final List<List<Path>> deletions = new ArrayList<>();

        assertThrows(SessionImportSiteAnonymization.SessionRemovedException.class,
                     () -> SessionImportSiteAnonymization.run(this::fail, SessionImportSiteAnonymization.removal(before, this::remove, deletions::add)));
        assertEquals("the session should be removed once", 1, removals.get());
        assertTrue("nothing should be deleted file by file", deletions.isEmpty());
    }

    @Test
    public void aMergeKeepsTheInstancesThatWereThereBefore() throws Exception {
        final File session = folder.newFolder("session");
        final File earlier = file(session, "SCANS/1/DICOM/earlier.dcm", "an instance from an earlier upload");
        final File catalog = file(session, "SCANS/1/DICOM/scan_1_catalog.xml", "<catalog/>");
        final SessionImportSiteAnonymization.SessionFiles before = SessionImportSiteAnonymization.SessionFiles.of(session);
        final File added = file(session, "SCANS/2/DICOM/added.dcm", "an instance from this upload");
        Files.write(catalog.toPath(), "<catalog>merged</catalog>".getBytes(StandardCharsets.UTF_8));

        final Exception thrown = assertThrows(Exception.class, () -> SessionImportSiteAnonymization.run(this::fail,
                SessionImportSiteAnonymization.removal(before, this::remove, files -> files.forEach(path -> path.toFile().delete()))));

        assertSame("the script's failure should surface, with the session kept", SCRIPT_FAILURE, thrown);
        assertEquals("the session should not be removed", 0, removals.get());
        assertTrue("an instance that was there before should stay", earlier.exists());
        assertFalse("an instance this upload wrote should be deleted", added.exists());
        assertTrue("a catalog is not an instance", catalog.exists());
    }

    @Test
    public void anInstanceTheUploadRewroteCountsAsWritten() throws Exception {
        final File session = folder.newFolder("rewritten");
        final File instance = file(session, "SCANS/1/DICOM/1.dcm", "an earlier version");
        final SessionImportSiteAnonymization.SessionFiles before = SessionImportSiteAnonymization.SessionFiles.of(session);
        Files.write(instance.toPath(), "the version this upload wrote over it".getBytes(StandardCharsets.UTF_8));

        assertEquals(Collections.singletonList(instance.toPath()), before.writtenSince());
    }

    @Test
    public void aSessionWhoseEveryInstanceWasRejectedIsRemoved() throws Exception {
        final List<AnonymizationResult> results = Arrays.asList(new AnonymizationResultReject(null, "rejected 1.dcm"), new AnonymizationResultReject(null, "rejected 2.dcm"));
        assertTrue(SessionImportSiteAnonymization.removeIfEveryInstanceRejected(results, this::remove));
        assertEquals("the session should be removed once", 1, removals.get());
    }

    @Test
    public void aSessionWithAnInstanceTheScriptKeptGoesOn() throws Exception {
        assertFalse(SessionImportSiteAnonymization.removeIfEveryInstanceRejected(Arrays.asList(new AnonymizationResultReject(null, "rejected 1.dcm"), new AnonymizationResultNoOp(null, "kept 2.dcm")), this::remove));
        assertFalse(SessionImportSiteAnonymization.removeIfEveryInstanceRejected(Collections.emptyList(), this::remove));
        assertEquals("the session should not be removed", 0, removals.get());
    }

    @Test
    public void aFailedRemovalAfterEveryRejectionFailsTheImport() {
        final Exception removalFailure = new IllegalStateException("prearchive unavailable");
        final Exception thrown = assertThrows(Exception.class, () -> SessionImportSiteAnonymization.removeIfEveryInstanceRejected(
                Collections.singletonList(new AnonymizationResultReject(null, "rejected 1.dcm")), () -> {
                    throw removalFailure;
                }));
        assertSame(removalFailure, thrown);
    }

    private static File file(final File directory, final String relativePath, final String content) throws IOException {
        final File file = new File(directory, relativePath);
        Files.createDirectories(file.getParentFile().toPath());
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    private List<AnonymizationResult> fail() throws MizerException {
        throw SCRIPT_FAILURE;
    }

    private boolean remove() {
        removals.incrementAndGet();
        return true;
    }
}
