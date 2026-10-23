/*
 * mizer: org.nrg.dicom.mizer.exceptions.MizerExceptionTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.mizer.exceptions;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MizerExceptionTests {

    @Test
    public void describeMessageReturnsMessageWhenPresent() {
        assertEquals("boom", MizerException.describeMessage(new IOException("boom")));
    }

    @Test
    public void describeMessageFallsBackToClassNameForNullMessage() {
        assertEquals("IOException", MizerException.describeMessage(new IOException()));
    }

    @Test
    public void describeMessageFallsBackToClassNameForEmptyMessage() {
        assertEquals("RuntimeException", MizerException.describeMessage(new RuntimeException("")));
    }

    @Test
    public void formatCauseChainReturnsEmptyForNoCauses() {
        final RuntimeException simple = new RuntimeException("just one");
        assertEquals("", MizerException.formatCauseChain(simple, "just one"));
    }

    @Test
    public void formatCauseChainEmitsCausedByLines() {
        final IOException inner = new IOException("disk full");
        final RuntimeException middle = new RuntimeException("write failed", inner);
        final RuntimeException outer = new RuntimeException("save failed", middle);
        final String chain = MizerException.formatCauseChain(outer, "save failed");
        assertTrue(chain, chain.contains("\ncaused by: write failed"));
        assertTrue(chain, chain.contains("\ncaused by: disk full"));
    }

    @Test
    public void formatCauseChainSkipsRedundantSubstrings() {
        // top message already contains the inner detail; the cause line should not repeat it.
        final RuntimeException inner = new RuntimeException("bad pattern");
        final RuntimeException outer = new RuntimeException("Error evaluating format (bad pattern)", inner);
        final String chain = MizerException.formatCauseChain(outer, "Error evaluating format (bad pattern)");
        assertEquals("", chain);
    }

    @Test
    public void formatCauseChainHandlesNull() {
        assertEquals("", MizerException.formatCauseChain(null, ""));
    }

    @Test
    public void describeMessageNeverNullForAnyThrowable() {
        try {
            MizerException.describeMessage(new RuntimeException());
        } catch (NullPointerException npe) {
            fail("describeMessage must not throw NPE on a Throwable with null message");
        }
    }

    @Test
    public void describeMessageNeverEmpty() {
        assertFalse(MizerException.describeMessage(new RuntimeException()).isEmpty());
        assertFalse(MizerException.describeMessage(new RuntimeException("")).isEmpty());
        assertFalse(MizerException.describeMessage(new RuntimeException("msg")).isEmpty());
    }
}
