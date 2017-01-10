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
    private static final ImmutableSortedSet<Integer> tags = ImmutableSortedSet.of();
    private final String name;
    private XnatProjectdata project;
    
    public FixedDicomProjectIdentifier(final XnatProjectdata project) {
        this.project = project;
        this.name = project.getName();
    }
    
    public FixedDicomProjectIdentifier(final String name) {
	this.project = null;
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.id.DicomObjectFunction#getTags()
     */
    @Override
    public SortedSet<Integer> getTags() { return tags; }

    /* (non-Javadoc)
     * @see org.nrg.dcm.id.DicomProjectIdentifier#apply(org.nrg.xdat.security.UserI, org.dcm4che2.data.DicomObject)
     */
    @Override
    public XnatProjectdata apply(final UserI user, final DicomObject o) {
	if (null == project) {
	    project = XnatProjectdata.getProjectByIDorAlias(name, user, false);
	}
	return project;
    }
}
