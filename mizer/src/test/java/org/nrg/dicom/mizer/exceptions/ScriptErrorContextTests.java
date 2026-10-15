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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ScriptErrorContextTests {

    @Test
    public void emptyContextRendersAsEmptyString() {
        assertEquals("", ScriptErrorContext.empty().format());
        assertTrue(ScriptErrorContext.empty().isEmpty());
    }

    @Test
    public void nullValuedWitherIsNoOp() {
        final ScriptErrorContext ctx = ScriptErrorContext.empty().withFilePath("/tmp/in.dcm");
        assertSame(ctx, ctx.withFilePath(null));
        assertSame(ctx, ctx.withScriptPath(null));
        assertSame(ctx, ctx.withScriptIndex(null));
        assertSame(ctx, ctx.withScriptLine(null));
        assertSame(ctx, ctx.withScriptColumn(null));
        assertSame(ctx, ctx.withStatementText(null));
        assertSame(ctx, ctx.withTag(null));
    }

    @Test
    public void nonNullWitherReturnsNewInstance() {
        final ScriptErrorContext base = ScriptErrorContext.empty();
        final ScriptErrorContext withFile = base.withFilePath("/tmp/in.dcm");
        assertNotSame(base, withFile);
        assertEquals("/tmp/in.dcm", withFile.getFilePath());
        assertTrue(base.isEmpty());
    }

    @Test
    public void formatRendersFilePath() {
        final String s = ScriptErrorContext.empty().withFilePath("/tmp/in.dcm").format();
        assertEquals("file=/tmp/in.dcm", s);
    }

    @Test
    public void formatRendersScriptIndexAndPath() {
        final String s = ScriptErrorContext.empty()
                .withScriptIndex(2)
                .withScriptPath("anon.das")
                .format();
        assertEquals("script #2 (anon.das)", s);
    }

    @Test
    public void formatRendersLineAndColumn() {
        final String s = ScriptErrorContext.empty()
                .withScriptLine(5)
                .withScriptColumn(7)
                .format();
        assertEquals("line 5:7", s);
    }

    @Test
    public void formatRendersTagInDicomHexNotation() {
        final String s = ScriptErrorContext.empty().withTag(0x00100010).format();
        assertEquals("tag (0010,0010)", s);
    }

    @Test
    public void formatTruncatesLongStatement() {
        final StringBuilder big = new StringBuilder();
        for (int i = 0; i < 200; i++) big.append('x');
        final String s = ScriptErrorContext.empty().withStatementText(big.toString()).format();
        // 120-char cap with ellipsis
        assertTrue("expected truncation: " + s, s.contains("…"));
        assertTrue(s.length() < big.length() + 10);
    }

    @Test
    public void formatJoinsAllFieldsWithSemicolons() {
        final String s = ScriptErrorContext.empty()
                .withFilePath("/tmp/in.dcm")
                .withScriptIndex(2)
                .withScriptPath("anon.das")
                .withScriptLine(5)
                .withScriptColumn(7)
                .withTag(0x00100010)
                .withStatementText("foo := bar")
                .format();
        assertTrue(s, s.contains("file=/tmp/in.dcm"));
        assertTrue(s, s.contains("script #2 (anon.das)"));
        assertTrue(s, s.contains("line 5:7"));
        assertTrue(s, s.contains("tag (0010,0010)"));
        assertTrue(s, s.contains("stmt=`foo := bar`"));
        // Five logical sections separated by "; ": file, script-id, line, tag, stmt
        assertEquals(5, s.split("; ").length);
    }

    @Test
    public void equalsAndHashCodeMatchOnSameFields() {
        final ScriptErrorContext a = ScriptErrorContext.empty().withScriptIndex(1).withScriptLine(3);
        final ScriptErrorContext b = ScriptErrorContext.empty().withScriptIndex(1).withScriptLine(3);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void notEqualWhenAFieldDiffers() {
        final ScriptErrorContext a = ScriptErrorContext.empty().withScriptIndex(1);
        final ScriptErrorContext b = ScriptErrorContext.empty().withScriptIndex(2);
        assertFalse(a.equals(b));
    }
}
