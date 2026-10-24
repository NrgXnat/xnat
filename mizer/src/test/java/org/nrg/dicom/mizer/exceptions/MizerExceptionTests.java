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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
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
    public void rootCauseMessageReturnsDeepestNonEmptyMessage() {
        final Throwable inner = new IllegalArgumentException("bad pattern: %z");
        final Throwable middle = new RuntimeException("Error evaluating format", inner);
        final Throwable outer = new RuntimeException("script error", middle);
        assertEquals("bad pattern: %z", MizerException.rootCauseMessage(outer));
    }

    @Test
    public void rootCauseMessageHandlesSelfReferenceWithoutInfiniteLoop() {
        // Throwable.initCause rejects self-reference, so use a custom Throwable that returns
        // itself from getCause() to exercise the production code's cycle guard.
        final RuntimeException self = new RuntimeException("loop") {
            private static final long serialVersionUID = 1L;
            @Override
            public synchronized Throwable getCause() {
                return this;
            }
        };
        assertEquals("loop", MizerException.rootCauseMessage(self));
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

    @Test
    public void contextExceptionRetainsTheScriptErrorContextObjectNotJustItsText() {
        // The applicator renders the context into the message, which suits a log or an HTTP body. A
        // caller that needs the line on its own -- the C-STORE responder, for a 64-character Error
        // Comment -- must not have to parse it back out of prose.
        final ScriptErrorContext ctx = ScriptErrorContext.empty()
                .withScriptLine(11)
                .withStatementText("(0010,0010):=subject");
        final MizerContextException wrapped = located(new RuntimeException("Variable 'subject' is null."), ctx);

        assertSame(ctx, wrapped.getScriptErrorContext());
        assertEquals("line 11", wrapped.getScriptErrorContext().formatCompact());
    }

    @Test
    public void findScriptErrorContextWalksTheChainOutermostFirst() {
        final ScriptErrorContext inner = ScriptErrorContext.empty().withScriptLine(11);
        final ScriptErrorContext outer = ScriptErrorContext.empty().withScriptLine(4);

        final MizerContextException wrappedOnce = located(new RuntimeException("boom"), inner);
        final MizerContextException wrappedTwice = located(wrappedOnce, outer);

        // The outermost boundary knows the most, so it wins.
        assertSame(outer, MizerContextException.findScriptErrorContext(wrappedTwice));
        assertSame(inner, MizerContextException.findScriptErrorContext(wrappedOnce));
    }

    @Test
    public void findScriptErrorContextReachesThroughForeignWrappers() {
        // The real chain is ClientException -> ServerException -> ... -> MizerContextException,
        // so the walk must not stop at the first non-mizer link.
        final ScriptErrorContext ctx = ScriptErrorContext.empty().withScriptLine(11);
        final Throwable chained = new IllegalStateException("outer",
                new IOException("middle", located(new RuntimeException("boom"), ctx)));
        assertSame(ctx, MizerContextException.findScriptErrorContext(chained));
    }

    @Test
    public void findScriptErrorContextReturnsNullWhenThereIsNone() {
        assertNull(MizerContextException.findScriptErrorContext(new IOException("boom")));
        assertNull(MizerContextException.findScriptErrorContext(null));
        assertNull(MizerContextException.findScriptErrorContext(located(new RuntimeException("boom"), null)));
        // An empty context carries no information, so it does not count as found.
        assertNull(MizerContextException.findScriptErrorContext(
                located(new RuntimeException("boom"), ScriptErrorContext.empty())));
    }

    @Test
    public void findScriptErrorContextSurvivesASelfReferentialCause() {
        final RuntimeException loop = new RuntimeException("loop") {
            private static final long serialVersionUID = 1L;
            @Override
            public synchronized Throwable getCause() {
                return this;
            }
        };
        assertNull(MizerContextException.findScriptErrorContext(loop));
    }

    private static MizerContextException located(final Throwable cause, final ScriptErrorContext ctx) {
        return new MizerContextException(null, ctx, MizerException.describeMessage(cause), cause);
    }
}
