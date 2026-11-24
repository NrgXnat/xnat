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

    private static String truncate(final String s, final int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max - 1) + "…";
    }
}
