/*
 * DicomEdit: org.nrg.dcm.edit.ConstraintRegexpMatch
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
class ConstraintRegexpMatch extends SimpleConstraintMatch {
    ConstraintRegexpMatch(final int tag, final String pattern) {
	super(tag, pattern);
    }
    
    ConstraintRegexpMatch(final Integer tag, final String pattern) {
	this(tag.intValue(), pattern);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.SimpleConstraintMatch#matches(java.lang.String)
     */
    public boolean matches(final String value) {
	return value.matches(getPattern());
    }
}
