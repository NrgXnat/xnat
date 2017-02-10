/*
 * web: org.nrg.dcm.id.FixedDicomProjectIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import java.util.SortedSet;

import org.dcm4che2.data.DicomObject;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;

import com.google.common.collect.ImmutableSortedSet;

public final class FixedDicomProjectIdentifier implements DicomProjectIdentifier {
    public FixedDicomProjectIdentifier(final XnatProjectdata project) {
        _project = project;
        _name = project.getName();
    }

    public FixedDicomProjectIdentifier(final String name) {
        _project = null;
        _name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Integer> getTags() {
        return TAGS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public XnatProjectdata apply(final UserI user, final DicomObject o) {
        if (null == _project) {
            _project = XnatProjectdata.getProjectByIDorAlias(_name, user, false);
        }
        return _project;
    }

    private static final ImmutableSortedSet<Integer> TAGS = ImmutableSortedSet.of();
    private final String          _name;
    private       XnatProjectdata _project;
}
