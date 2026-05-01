/*
 * DicomEdit: org.nrg.dcm.edit.Echo
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.Variable;

import java.util.Map;
import java.util.SortedSet;


/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class Echo implements Operation {
    private final Value v;

    public Echo(final String message) {
        this(new ConstantValue(message));
    }

    public Echo(final Value v) {
        this.v = v;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#affects(int)
     */
    public boolean affects(final int affects) { return false; }

    /* (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#makeAction(org.dcm4che2.data.DicomObject)
     */
    public Action makeAction(final DicomObjectI dicomObject) {
        String m;
        try {
            m = v.on(dicomObject);
        } catch (ScriptEvaluationException e) {
            m = "[uninterpretable: " + v + "]";
        }
        final String message = m;
        return new Action() {
            public void apply() { System.out.print(message); }
            public String toString() { return getName() + ": " + message; }
        };
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#getTopTag()
     */
    public long getTopTag() {
        final SortedSet<Long> tags = getScriptTags();
        return tags.isEmpty() ? 0 : tags.last();
    }

    @Override
    public SortedSet<Long> getScriptTags() {
        return v.getTags();
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#apply(java.util.Map)
     */
    public String apply(final Map<Integer,String> values) {
        String m;
        try {
            m = v.on(values);
        } catch (ScriptEvaluationException e) {
            m = "[uninterpretable: " + v + "]";
        }
        System.out.print(m);
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#replace(org.nrg.dcm.edit.Variable)
     */
    public void replace(final Variable var) {
        v.replace(var);
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() { return v.toString(); }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.edit.Operation#getName()
     */
    public String getName() { return "Echo"; }
}
