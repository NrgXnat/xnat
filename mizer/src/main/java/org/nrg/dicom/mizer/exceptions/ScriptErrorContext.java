/*
 * mizer: org.nrg.dicom.mizer.exceptions.ScriptErrorContext
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.mizer.exceptions;

import java.util.Objects;

/**
 * Immutable bundle of optional location/identifier fields that pinpoint where in an
 * anonymization run an error originated. Designed to be attached to a thrown exception
 * (typically via {@link MizerException#rewrap}) so the user-facing message can include
 * file path, script index/line, statement text, and DICOM tag information.
 *
 * <p>All fields are optional. Use the fluent {@code with*} methods to build up a context;
 * each call returns a new instance.
 */
public final class ScriptErrorContext {

    public static final ScriptErrorContext EMPTY = new ScriptErrorContext(null, null, null, null, null, null, null);

    private final String  filePath;
    private final String  scriptPath;
    private final Integer scriptIndex;
    private final Integer scriptLine;
    private final Integer scriptColumn;
    private final String  statementText;
    private final Integer tag;

    private ScriptErrorContext(final String filePath,
                               final String scriptPath,
                               final Integer scriptIndex,
                               final Integer scriptLine,
                               final Integer scriptColumn,
                               final String statementText,
                               final Integer tag) {
        this.filePath      = filePath;
        this.scriptPath    = scriptPath;
        this.scriptIndex   = scriptIndex;
        this.scriptLine    = scriptLine;
        this.scriptColumn  = scriptColumn;
        this.statementText = statementText;
        this.tag           = tag;
    }

    public static ScriptErrorContext empty() {
        return EMPTY;
    }

    public ScriptErrorContext withFilePath(final String value) {
        if (value == null) return this;
        return new ScriptErrorContext(value, scriptPath, scriptIndex, scriptLine, scriptColumn, statementText, tag);
    }

    public ScriptErrorContext withScriptPath(final String value) {
        if (value == null) return this;
        return new ScriptErrorContext(filePath, value, scriptIndex, scriptLine, scriptColumn, statementText, tag);
    }

    public ScriptErrorContext withScriptIndex(final Integer value) {
        if (value == null) return this;
        return new ScriptErrorContext(filePath, scriptPath, value, scriptLine, scriptColumn, statementText, tag);
    }

    public ScriptErrorContext withScriptLine(final Integer value) {
        if (value == null) return this;
        return new ScriptErrorContext(filePath, scriptPath, scriptIndex, value, scriptColumn, statementText, tag);
    }

    public ScriptErrorContext withScriptColumn(final Integer value) {
        if (value == null) return this;
        return new ScriptErrorContext(filePath, scriptPath, scriptIndex, scriptLine, value, statementText, tag);
    }

    public ScriptErrorContext withStatementText(final String value) {
        if (value == null) return this;
        return new ScriptErrorContext(filePath, scriptPath, scriptIndex, scriptLine, scriptColumn, value, tag);
    }

    public ScriptErrorContext withTag(final Integer value) {
        if (value == null) return this;
        return new ScriptErrorContext(filePath, scriptPath, scriptIndex, scriptLine, scriptColumn, statementText, value);
    }

    public String  getFilePath()      { return filePath;      }
    public String  getScriptPath()    { return scriptPath;    }
    public Integer getScriptIndex()   { return scriptIndex;   }
    public Integer getScriptLine()    { return scriptLine;    }
    public Integer getScriptColumn()  { return scriptColumn;  }
    public String  getStatementText() { return statementText; }
    public Integer getTag()           { return tag;           }

    /**
     * @return {@code true} if every field is null. An empty context renders as the empty string.
     */
    public boolean isEmpty() {
        return filePath == null
                && scriptPath == null
                && scriptIndex == null
                && scriptLine == null
                && scriptColumn == null
                && statementText == null
                && tag == null;
    }

    /**
     * Render the populated fields into a stable, single-line prefix suitable for
     * prepending to an exception message. Empty contexts return an empty string.
     *
     * <p>Example: {@code file=/tmp/in.dcm; script #2 (anon.das); line 5:7; tag (0010,0010); stmt=`(0010,0010) := "anon"`}
     */
    public String format() {
        if (isEmpty()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        appendField(sb, "file",   filePath);
        appendScriptId(sb);
        appendLineColumn(sb);
        appendTag(sb);
        appendStatement(sb);
        return sb.toString();
    }

    private void appendScriptId(final StringBuilder sb) {
        if (scriptIndex == null && scriptPath == null) {
            return;
        }
        appendSeparator(sb);
        sb.append("script");
        if (scriptIndex != null) {
            sb.append(" #").append(scriptIndex);
        }
        if (scriptPath != null) {
            sb.append(" (").append(scriptPath).append(')');
        }
    }

    private void appendLineColumn(final StringBuilder sb) {
        if (scriptLine == null && scriptColumn == null) {
            return;
        }
        appendSeparator(sb);
        sb.append("line ");
        if (scriptLine != null) {
            sb.append(scriptLine);
        } else {
            sb.append('?');
        }
        if (scriptColumn != null) {
            sb.append(':').append(scriptColumn);
        }
    }

    private void appendTag(final StringBuilder sb) {
        if (tag == null) {
            return;
        }
        appendSeparator(sb);
        final int group   = (tag >>> 16) & 0xFFFF;
        final int element = tag          & 0xFFFF;
        sb.append(String.format("tag (%04X,%04X)", group, element));
    }

    private void appendStatement(final StringBuilder sb) {
        if (statementText == null) {
            return;
        }
        appendSeparator(sb);
        sb.append("stmt=`").append(truncate(statementText, 120)).append('`');
    }

    private static void appendField(final StringBuilder sb, final String name, final String value) {
        if (value == null) {
            return;
        }
        appendSeparator(sb);
        sb.append(name).append('=').append(value);
    }

    private static void appendSeparator(final StringBuilder sb) {
        if (sb.length() > 0) {
            sb.append("; ");
        }
    }

    private static String truncate(final String s, final int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max - 1) + "…";
    }

    @Override
    public String toString() {
        return isEmpty() ? "ScriptErrorContext{}" : "ScriptErrorContext{" + format() + "}";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ScriptErrorContext)) return false;
        final ScriptErrorContext that = (ScriptErrorContext) o;
        return Objects.equals(filePath, that.filePath)
                && Objects.equals(scriptPath, that.scriptPath)
                && Objects.equals(scriptIndex, that.scriptIndex)
                && Objects.equals(scriptLine, that.scriptLine)
                && Objects.equals(scriptColumn, that.scriptColumn)
                && Objects.equals(statementText, that.statementText)
                && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, scriptPath, scriptIndex, scriptLine, scriptColumn, statementText, tag);
    }
}
