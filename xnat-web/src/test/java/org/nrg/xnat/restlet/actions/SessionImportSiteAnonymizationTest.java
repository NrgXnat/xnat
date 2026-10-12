package org.nrg.xnat.restlet.actions;

import org.junit.Test;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultNoOp;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

/**
 * A session import's site script runs after the upload is unpacked into the prearchive. When it fails, the session
 * must not stay there with its instances as they arrived.
 */
public class SessionImportSiteAnonymizationTest {
    private static final MizerException SCRIPT_FAILURE = new MizerException("Unable to anonymize 1.dcm");

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

    private List<AnonymizationResult> fail() throws MizerException {
        throw SCRIPT_FAILURE;
    }

    private boolean remove() {
        removals.incrementAndGet();
        return true;
    }
}
