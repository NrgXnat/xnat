/*
 * mizer: org.nrg.dicom.mizer.values.ConstantValue
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.mizer.values;

import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.variables.Variable;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class ConstantValue extends AbstractMizerValue {
    public ConstantValue() {
        super();
    }

    public ConstantValue(final Object value) {
        super(value);
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<Long> getTags() {
        return EMPTY_TAGS;
    }

    /**
     * {@inheritDoc}
     */
    public Set<Variable> getVariables() {
        return EMPTY_VARIABLES;
    }

    /**
     * {@inheritDoc}
     */
    public String on(final DicomObjectI dicomObject) {
        return asString();
    }

    /**
     * {@inheritDoc}
     */
    public String on(final Map<Integer, String> map) {
        return asString();
    }

    /**
     * {@inheritDoc}
     */
    public void replace(final Variable variable) {
        //
    }
}
