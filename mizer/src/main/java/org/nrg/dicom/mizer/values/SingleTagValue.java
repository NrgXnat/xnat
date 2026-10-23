/*
 * DicomEdit: org.nrg.dicom.mizer.values.SingleTagValue
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.mizer.values;

import com.google.common.collect.ImmutableSortedSet;
import org.dcm4che3.util.TagUtils;
import org.dcm4che3.data.Attributes;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.variables.Variable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class SingleTagValue extends AbstractMizerValue {
    public SingleTagValue(final int tag) {
        _tag = tag;
    }

    public SingleTagValue(@Nonnull final Integer tag) { this(tag.intValue()); }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Value#getTags()
     */
    public SortedSet<Long> getTags() {
        return ImmutableSortedSet.of(_tag);
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Value#getVariables()
     */
    public Set<Variable> getVariables() {
        return Collections.emptySet();
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.edit.Value#on(org.nrg.dicom.mizer.objects.DicomObjectI)
     */
    public String on(final DicomObjectI dicomObject)
            throws ScriptEvaluationException {
        try {
            Attributes attrs = dicomObject.getAttributes();
            String value = attrs.getString((int)_tag); 
            return value;
        } catch (Exception e) {
            throw new ScriptEvaluationException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Value#on(java.util.Map)
     */
    public String on(final Map<Integer,String> m) {
        return m.get(Long.valueOf(_tag).intValue());
    }
    
    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Value#replaceVariable(org.nrg.dcm.edit.Variable)
     */
    public void replace(final Variable variable) {}

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(final Object o) {
        return o instanceof SingleTagValue && _tag == ((SingleTagValue) o)._tag;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (int) _tag;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "value-of" + TagUtils.toString((int) _tag);
    }

    private final long _tag;
}
