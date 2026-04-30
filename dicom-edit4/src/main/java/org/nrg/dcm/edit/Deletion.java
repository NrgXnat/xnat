/*
 * DicomEdit: org.nrg.dcm.edit.Deletion
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import com.google.common.collect.Sets;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.variables.Variable;
import org.nrg.dicomtools.exceptions.AttributeException;

import java.util.Map;
import java.util.Set;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class Deletion extends AbstractOperation {
    private final TagPattern pattern;

    public Deletion(final TagPattern pattern) {
        super("Delete");
        this.pattern = pattern;
    }

    public Deletion(final int tag) {
        this(new TagPattern(tag));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#affects(int)
     */
    public boolean affects(final int tag) {
        return pattern.apply(tag);
    }

    public Action makeAction(final DicomObjectI dicomObject) throws AttributeException {
        return new Action() {
            public void apply() {
                final Iterable<Long> iterable       = pattern.apply(dicomObject);
                if (iterable != null) {
                    for (final long tag : iterable) {
                        dicomObject.delete((int)tag);
                    }
                }
            }

            public String toString() {
                return getName() + " " + pattern;
            }
        };
    }

    public String apply(final Map<Integer,String> values) { return null; }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#getTopTag()
     */
    public long getTopTag() { return pattern.getTopTag(); }

    @Override
    public Set<Long> getScriptTags() {
        return Sets.newHashSet(getTopTag());
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#replace(org.nrg.dcm.edit.Variable)
     */
    public void replace(final Variable variable) {}

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Delete " + pattern;
    }
}
