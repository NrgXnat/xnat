/*
 * dicom-xnat-util: org.nrg.dcm.ContainedAssignmentExtractor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import java.util.regex.Pattern;

/**
 * 
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class ContainedAssignmentExtractor extends MatchedPatternExtractor
implements Extractor {
    private final static String START = "(?:\\A|(?:.*[\\s,;]))", OPTWS = "\\s*", END = "(?:(?:[\\s,;].*\\Z)|\\Z)",
    START_GROUP = "(", END_GROUP = ")";
    private final static String DEFAULT_VALUE_PATTERN = "[\\w\\-]*";
    private final static String DEFAULT_OP = "\\:";

    public ContainedAssignmentExtractor(final int tag,
            final String id, final String op, final String valuePattern,
            int patternFlags) {
        super(tag, Pattern.compile(new StringBuilder(START)
        .append(id)
        .append(OPTWS).append(op).append(OPTWS)
        .append(START_GROUP).append(valuePattern).append(END_GROUP)
        .append(END)
        .toString(), patternFlags), 1);
    }

    public ContainedAssignmentExtractor(final int tag, final String id, final String op, final int patternFlags) {
        this(tag, id, op, DEFAULT_VALUE_PATTERN, patternFlags);
    }

    public ContainedAssignmentExtractor(final int tag, final String id, final String op) {
        this(tag, id, op, 0);
    }

    public ContainedAssignmentExtractor(final int tag, final String id, final int patternFlags) {
        this(tag, id, DEFAULT_OP, DEFAULT_VALUE_PATTERN, patternFlags);
    }

    public ContainedAssignmentExtractor(final int tag, final String id) {
        this(tag, id, 0);
    }
}
