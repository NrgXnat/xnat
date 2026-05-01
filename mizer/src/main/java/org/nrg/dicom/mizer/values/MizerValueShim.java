/*
 * mizer: org.nrg.dicom.mizer.values.MizerValueShim
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.values;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.variables.Variable;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * Provides a shim that can contain a {@link Value} instance and access the basic metadata for the value.
 */
public abstract class MizerValueShim extends AbstractMizerValue {
    public MizerValueShim(final Value value) {
        super(value.asString());
        _value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String on(final DicomObjectI dicomObject) throws ScriptEvaluationException {
        return _value.on(dicomObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String on(final Map<Integer, String> map) throws ScriptEvaluationException {
        return _value.on(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Long> getTags() {
        return _value.getTags();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Variable> getVariables() {
        return _value.getVariables();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(final Variable variable) {
        _value.replace(variable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getValue().toString();
    }

    /**
     * Returns the value stored internally.
     *
     * @return The internal value object.
     */
    protected Value getValue() {
        return _value;
    }

    private final Value _value;
}
