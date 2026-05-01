/*
 * DicomEdit: org.nrg.dcm.edit.ValueEqualsConstraint
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import com.google.common.base.Objects;
import org.nrg.dicom.mizer.values.Value;


/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
final class ValueEqualsConstraint extends AbstractValueConstraint implements ConstraintMatch {
    ValueEqualsConstraint(final Value left, Value right) {
        super(left, right);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.AbstractValueConstraint#matches(java.lang.String, java.lang.String)
     */
    protected boolean matches(final String left, final String right) {
        return Objects.equal(left, right);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return toString("equals");
    }
}
