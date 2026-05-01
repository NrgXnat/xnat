/*
 * DicomEdit: org.nrg.dcm.edit.NoOp
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import com.google.common.collect.ImmutableSet;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.variables.Variable;
import org.nrg.dicomtools.exceptions.AttributeException;

import java.util.Map;
import java.util.Set;

/**
 * Null operation
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public class NoOp extends AbstractOperation {
    private static final String id = "NoOp";
    private static final Set<Long> tags = ImmutableSet.of(0L);

    public NoOp() { super(id); }

    public Action makeAction(final DicomObjectI dicomObject) throws AttributeException {
        return new Action() {
            public void apply() {}
            public String toString() { return id; }
        };
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#affects(int)
     */
    public boolean affects(final int affects) { return false; }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#apply(java.util.Map)
     */
    public String apply(Map<Integer,String> values) {
        return null;
    }

    /*
     * all NoOp objects are equivalent
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) { return o instanceof NoOp; }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#getTopTag()
     */
    public long getTopTag() { return 0; }

    public Set<Long> getScriptTags() {
        return tags;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#replace(org.nrg.dcm.edit.Variable)
     */
    public void replace(final Variable variable) {}
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() { return 41; }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() { return id; }
}
