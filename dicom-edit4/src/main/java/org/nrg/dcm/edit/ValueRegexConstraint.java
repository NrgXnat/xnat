/*
 * DicomEdit: org.nrg.dcm.edit.ValueRegexConstraint
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.values.Value;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
final class ValueRegexConstraint extends AbstractValueConstraint {
    public ValueRegexConstraint(final Value value, final Value pattern) {
        super(value, pattern);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.ValueConstraintMatch#matches(java.lang.String, java.lang.String)
     */
    protected boolean matches(final String value, final String pattern) throws ScriptEvaluationException {
        if (null == pattern) {
            throw new ScriptEvaluationException("null pattern not allowed");
        }
        return null != value && value.matches(pattern);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return toString("matches");
    }
}
