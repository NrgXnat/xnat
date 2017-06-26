/*
 * web: org.nrg.dcm.id.ContainedAssignmentDicomIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class ContainedAssignmentDicomIdentifier extends DelegateDicomIdentifier {
    public ContainedAssignmentDicomIdentifier(final int tag, final String id, final String op, final String valuePattern, final int patternFlags) {
        super(new PatternDicomIdentifier(tag, getPattern(id, op, valuePattern, patternFlags), 1));
    }

    public ContainedAssignmentDicomIdentifier(final int tag, final String id, final String op, final int patternFlags) {
        this(tag, id, op, DEFAULT_VALUE_PATTERN, patternFlags);
    }

    @SuppressWarnings("unused")
    public ContainedAssignmentDicomIdentifier(final int tag, final String id, final String op) {
        this(tag, id, op, 0);
    }

    public ContainedAssignmentDicomIdentifier(final int tag, final String id, final int patternFlags) {
        this(tag, id, DEFAULT_OP, DEFAULT_VALUE_PATTERN, patternFlags);
    }

    @SuppressWarnings("unused")
    public ContainedAssignmentDicomIdentifier(final int tag, final String id) {
        this(tag, id, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getIdentifier().toString();
    }

    @NotNull
    private static Pattern getPattern(final String id, final String op, final String valuePattern, final int patternFlags) {
        return Pattern.compile(String.format(PATTERN, id, op, valuePattern), patternFlags);
    }

    private final static String PATTERN = "(?:\\A|(?:.*[\\s,;]))%s\\s*%s\\s*(%s)(?:(?:[\\s,;].*\\Z)|\\Z)";
    private final static String DEFAULT_VALUE_PATTERN = "[\\w\\-]*";
    private final static String DEFAULT_OP = "\\:";
}
