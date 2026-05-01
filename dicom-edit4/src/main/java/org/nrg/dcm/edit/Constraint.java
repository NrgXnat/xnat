/*
 * DicomEdit: org.nrg.dcm.edit.Constraint
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.collect.Sets;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;

/**
 * Represents a constraint on an Operation.
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class Constraint {
    public Constraint(final ConstraintMatch match) {
        this(match, empty);
    }

    public Constraint(final ConstraintMatch match, final File f) {
        this(match, Collections.singletonList(f));
    }

    public Constraint(final ConstraintMatch match, final Iterable<File> fs) {
        this._match = match;
        files = Sets.newHashSet(fs);
        if (null == match && files.isEmpty()) {
            throw new IllegalArgumentException("Either value constraint or file list must be provided");
        }
    }

    boolean matches(final File file, final DicomObjectI dicomObject) throws ScriptEvaluationException {
        assert _match != null || !files.isEmpty();
        return !(_match != null && !_match.matches(dicomObject)) && (files.isEmpty() || files.contains(file));
    }

    public SortedSet<Long> getTags() {
        return _match.getTags();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() { return _match.toString(); }

    private static final Iterable<File> empty = Collections.emptyList();
    private final ConstraintMatch _match;
    private final Set<File>       files;
}
