/*
 * mizer: org.nrg.dicom.mizer.exceptions.ScriptErrorContext
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.mizer.exceptions;

/**
 * Immutable bundle of optional location fields that pinpoint where in a script an error
 * originated. {@link #format()} renders it as the prefix of a user-facing message, so the
 * message can include the script line and statement text.
 *
 * <p>All fields are optional. Use the fluent {@code with*} methods to build up a context;
 * each call returns a new instance.
 */
public final class ScriptErrorContext {

    private static final ScriptErrorContext EMPTY = new ScriptErrorContext(null, null);

    private final Integer scriptLine;
    private final String  statementText;

    private ScriptErrorContext(final Integer scriptLine, final String statementText) {
        this.scriptLine    = scriptLine;
        this.statementText = statementText;
    }

    public static ScriptErrorContext empty() {
        return EMPTY;
    }

    public ScriptErrorContext withScriptLine(final Integer value) {
        if (value == null) return this;
        return new ScriptErrorContext(value, statementText);
    }

    public ScriptErrorContext withStatementText(final String value) {
        if (value == null) return this;
        return new ScriptErrorContext(scriptLine, value);
    }

    /**
     * @return whether no field is set, in which case {@link #format()} and {@link #formatCompact()} are both empty
     */
    public boolean isEmpty() {
        return scriptLine == null && statementText == null;
    }

    /**
     * Render the populated fields into a stable, single-line prefix suitable for
     * prepending to an exception message. Empty contexts return an empty string.
     *
     * <p>Example: {@code line 5; stmt=`(0010,0010):="anon"`}
     */
    public String format() {
        final StringBuilder sb = new StringBuilder();
        if (scriptLine != null) {
            sb.append("line ").append(scriptLine);
        }
        if (statementText != null) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append("stmt=`").append(truncate(statementText, 120)).append('`');
        }
        return sb.toString();
    }

    /**
     * Render the smallest useful locator, for channels with a hard length budget — notably the
     * DICOM C-STORE-RSP Error Comment (0000,0902), which PS3.4 Table B.2-1 permits on a failure
     * status but whose VR LO caps it at 64 characters, most of which the reason text needs.
     *
     * <p>Includes the line only. Deliberately omits the statement text, which is unbounded and
     * would consume the whole budget. Returns the empty string when no line is set.
     *
     * <p>Example: {@code line 11}
     */
    public String formatCompact() {
        return scriptLine == null ? "" : "line " + scriptLine;
    }

    private static String truncate(final String s, final int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max - 1) + "…";
    }
}
