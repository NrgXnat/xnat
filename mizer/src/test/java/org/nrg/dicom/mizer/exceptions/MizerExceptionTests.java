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
import static org.junit.Assert.assertNotNull;
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
    public void rewrapPreservesCauseAndPrependsContext() {
        final IOException inner = new IOException("disk full");
        final ScriptErrorContext ctx = ScriptErrorContext.empty()
                .withFilePath("/tmp/x.dcm")
                .withScriptLine(5);
        final MizerContextException decorated = MizerException.rewrap(inner, ctx);
        assertSame(inner, decorated.getCause());
        assertTrue(decorated.getMessage(), decorated.getMessage().startsWith("file=/tmp/x.dcm; line 5: "));
        assertTrue(decorated.getMessage(), decorated.getMessage().endsWith("disk full"));
    }

    @Test
    public void rewrapWithEmptyContextUsesCauseMessageAlone() {
        final IOException inner = new IOException("disk full");
        final MizerContextException decorated = MizerException.rewrap(inner, ScriptErrorContext.empty());
        assertEquals("disk full", decorated.getMessage());
        assertSame(inner, decorated.getCause());
    }

    @Test
    public void rewrapInheritsMizerContextFromCause() {
        // a MizerContextException with a (possibly null) MizerContext field
        final MizerContextException original = new MizerContextException(null, "first error");
        final MizerContextException decorated = MizerException.rewrap(original, ScriptErrorContext.empty().withScriptIndex(2));
        assertSame(original, decorated.getCause());
        assertTrue(decorated.getMessage(), decorated.getMessage().startsWith("script #2: "));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rewrapRejectsNullCause() {
        MizerException.rewrap(null, ScriptErrorContext.empty());
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
    public void rewrapMessageIsActionable() {
        // End-to-end shape check: caller catches at boundary, rewraps, surfaces message.
        final NumberFormatException inner = new NumberFormatException("For input string: \"abc\"");
        final ScriptErrorContext ctx = ScriptErrorContext.empty()
                .withScriptIndex(1)
                .withScriptLine(7)
                .withStatementText("age := parseInt(\"abc\")");
        final MizerContextException decorated = MizerException.rewrap(inner, ctx);
        final String msg = decorated.getMessage();
        assertTrue(msg, msg.contains("script #1"));
        assertTrue(msg, msg.contains("line 7"));
        assertTrue(msg, msg.contains("For input string: \"abc\""));
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
    public void rewrapRetainsTheScriptErrorContextObjectNotJustItsText() {
        // rewrap renders the context into the message, which suits a log or an HTTP body. A caller
        // that needs the fields separately -- the C-STORE responder needs the line for a 64-character
        // Error Comment and the tag as an int for Offending Element -- must not have to parse them
        // back out of prose.
        final ScriptErrorContext ctx = ScriptErrorContext.empty()
                .withScriptLine(11)
                .withTag(0x00100010)
                .withStatementText("(0010,0010):=subject");
        final MizerContextException wrapped =
                MizerException.rewrap(new RuntimeException("Variable 'subject' is null."), ctx);

        assertSame(ctx, wrapped.getScriptErrorContext());
        assertEquals(Integer.valueOf(11), wrapped.getScriptErrorContext().getScriptLine());
        assertEquals(Integer.valueOf(0x00100010), wrapped.getScriptErrorContext().getTag());
    }

    @Test
    public void findScriptErrorContextWalksTheChainOutermostFirst() {
        final ScriptErrorContext inner = ScriptErrorContext.empty().withScriptLine(11);
        final ScriptErrorContext outer = ScriptErrorContext.empty().withScriptLine(4).withScriptIndex(2);

        final MizerContextException wrappedOnce = MizerException.rewrap(new RuntimeException("boom"), inner);
        final MizerContextException wrappedTwice = MizerException.rewrap(wrappedOnce, outer);

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
                new IOException("middle", MizerException.rewrap(new RuntimeException("boom"), ctx)));
        assertSame(ctx, MizerContextException.findScriptErrorContext(chained));
    }

    @Test
    public void findScriptErrorContextReturnsNullWhenThereIsNone() {
        assertNull(MizerContextException.findScriptErrorContext(new IOException("boom")));
        assertNull(MizerContextException.findScriptErrorContext(null));
        // An empty context carries no information, so it does not count as found.
        assertNull(MizerContextException.findScriptErrorContext(
                MizerException.rewrap(new RuntimeException("boom"), ScriptErrorContext.empty())));
    }

    @Test
    public void findScriptErrorContextSurvivesASelfReferentialCause() {
        final RuntimeException loop = new RuntimeException("loop") {
            @Override
            public synchronized Throwable getCause() {
                return this;
            }
        };
        assertNull(MizerContextException.findScriptErrorContext(loop));
    }

    @Test
    public void rewrapWithoutAContextRetainsNone() {
        final MizerContextException wrapped = MizerException.rewrap(new RuntimeException("boom"), null);
        assertNull(wrapped.getScriptErrorContext());
        assertNotNull(wrapped.getMessage());
    }
}
