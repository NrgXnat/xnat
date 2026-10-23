/*
 * mizer: org.nrg.dicom.mizer.exceptions.ScriptErrorContextTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.mizer.exceptions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class ScriptErrorContextTests {

    @Test
    public void emptyContextRendersAsEmptyString() {
        assertEquals("", ScriptErrorContext.empty().format());
    }

    @Test
    public void nullValuedWitherIsNoOp() {
        final ScriptErrorContext ctx = ScriptErrorContext.empty().withScriptLine(5);
        assertSame(ctx, ctx.withScriptLine(null));
        assertSame(ctx, ctx.withStatementText(null));
    }

    @Test
    public void nonNullWitherReturnsNewInstance() {
        final ScriptErrorContext base = ScriptErrorContext.empty();
        final ScriptErrorContext withLine = base.withScriptLine(5);
        assertNotSame(base, withLine);
        assertEquals("line 5", withLine.format());
        assertEquals("", base.format());
    }

    @Test
    public void formatRendersStatementAlone() {
        assertEquals("stmt=`foo := bar`", ScriptErrorContext.empty().withStatementText("foo := bar").format());
    }

    @Test
    public void formatJoinsLineAndStatementWithASemicolon() {
        final String s = ScriptErrorContext.empty()
                .withScriptLine(5)
                .withStatementText("foo := bar")
                .format();
        assertEquals("line 5; stmt=`foo := bar`", s);
    }

    @Test
    public void formatTruncatesLongStatement() {
        final StringBuilder big = new StringBuilder();
        for (int i = 0; i < 200; i++) big.append('x');
        final String s = ScriptErrorContext.empty().withStatementText(big.toString()).format();
        // 120-char cap: 119 characters and an ellipsis
        assertEquals("stmt=`" + big.substring(0, 119) + "…`", s);
    }
}
