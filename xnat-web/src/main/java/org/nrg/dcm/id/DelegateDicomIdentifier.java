/*
 * web: org.nrg.dcm.id.DelegateDicomIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import org.dcm4che2.data.DicomObject;

import java.util.SortedSet;

public class DelegateDicomIdentifier implements DicomDerivedString {
    private final DicomDerivedString identifier;
    
    public DelegateDicomIdentifier(DicomDerivedString identifier) {
        this.identifier = identifier;
    }
    
    /* (non-Javadoc)
     * @see org.nrg.dcm.id.DicomObjectFunction#getTags()
     */
    public final SortedSet<Integer> getTags() { return identifier.getTags(); }

    /* (non-Javadoc)
     * @see com.google.common.base.Function#apply(java.lang.Object)
     */
    public final String apply(DicomObject o) { return identifier.apply(o); }
}
