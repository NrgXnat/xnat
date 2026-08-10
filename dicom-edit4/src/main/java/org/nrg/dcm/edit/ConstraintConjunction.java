/*
 * DicomEdit: org.nrg.dcm.edit.ConstraintConjunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;

import java.util.Collections;
import java.util.SortedSet;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class ConstraintConjunction implements ConstraintMatch {
    private final Iterable<ConstraintMatch> predicates;

    public ConstraintConjunction(final ConstraintMatch predicate) {
        this(Collections.singletonList(predicate));
    }

    public ConstraintConjunction(final Iterable<ConstraintMatch> predicates) {
        this.predicates = Lists.newArrayList(predicates);
    }

    public SortedSet<Long> getTags() {
        final SortedSet<Long> tags = Sets.newTreeSet();
        for (final ConstraintMatch p : predicates) {
            tags.addAll(p.getTags());   
        }
        return tags;
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.edit.ConstraintMatch#matches(org.nrg.dicom.mizer.objects.DicomObjectI)
     */
    public boolean matches(final DicomObjectI dicomObject) throws ScriptEvaluationException {
        for (final ConstraintMatch predicate : predicates) {
            if (!predicate.matches(dicomObject)) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("ConstraintConjunction: ");
        if (Iterables.isEmpty(predicates)) {
            sb.append("[TRUE]");
        } else {
            Joiner.on(" AND ").appendTo(sb, predicates);
        }
        return sb.toString();
    }

}
