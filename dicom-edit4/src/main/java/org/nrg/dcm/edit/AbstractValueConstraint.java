/*
 * DicomEdit: org.nrg.dcm.edit.AbstractValueConstraint
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import com.google.common.collect.ImmutableSortedSet;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.Value;

import java.util.SortedSet;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public abstract class AbstractValueConstraint implements ConstraintMatch {
    AbstractValueConstraint(final Value left, final Value right) {
        _left = left;
        _right = right;
    }

    abstract protected boolean matches(String pattern, String value) throws ScriptEvaluationException;

    public SortedSet<Long> getTags() {
        final ImmutableSortedSet.Builder<Long> tags = ImmutableSortedSet.naturalOrder();
        tags.addAll(_left.getTags());
        tags.addAll(_right.getTags());
        return tags.build();
    }

    final public boolean matches(final DicomObjectI dicomObject) throws ScriptEvaluationException {
        return matches(_left.on(dicomObject), _right.on(dicomObject));
    }

    public String toString(final String operation) {
        return "Constraint: " + _left + " " + operation + " " + _right;
    }

    private final Value _left, _right;
}
