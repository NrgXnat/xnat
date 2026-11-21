/*
 * mizer: org.nrg.dicom.mizer.objects.AnonymizationResultErrorTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.mizer.objects;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AnonymizationResultErrorTests {

    @Test
    public void messageWithoutCauseIsJustTheJoinedMessages() {
        final AnonymizationResultError result = new AnonymizationResultError(null, "boom");
        assertEquals("boom", result.getMessage());
        assertNull(result.getCause());
    }

    @Test
    public void messageWithCauseAppendsCausedByChain() {
        final IOException inner = new IOException("disk full");
        final RuntimeException outer = new RuntimeException("save failed", inner);
        final AnonymizationResultError result = new AnonymizationResultError(null, "save failed", outer);
        final String msg = result.getMessage();
        assertTrue(msg, msg.startsWith("save failed"));
        assertTrue(msg, msg.contains("\ncaused by: disk full"));
        assertSame(outer, result.getCause());
    }

    @Test
    public void messageDoesNotDuplicateInfoAlreadyInTopLine() {
        final RuntimeException inner = new RuntimeException("bad pattern");
        final RuntimeException outer = new RuntimeException("Error evaluating format (bad pattern)", inner);
        final AnonymizationResultError result = new AnonymizationResultError(null,
                "Error evaluating format (bad pattern)", outer);
        // Inner cause's "bad pattern" is already a substring of the top-level message.
        // The chain should not re-emit it.
        final String msg = result.getMessage();
        assertEquals("Error evaluating format (bad pattern)", msg);
        assertFalse(msg, msg.contains("caused by"));
    }

    @Test
    public void severityIsAlwaysError() {
        final AnonymizationResultError result = new AnonymizationResultError(null, "x");
        assertEquals(AnonymizationResultSeverity.ERROR, result.getSeverity());
    }
}
