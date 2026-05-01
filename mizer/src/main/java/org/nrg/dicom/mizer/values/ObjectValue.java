/*
 * mizer: org.nrg.dicom.mizer.values.ObjectValue
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dicom.mizer.values;

import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.variables.Variable;
import org.nrg.framework.utilities.Reflection;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * Provides a generic typed value instance.
 */
public class ObjectValue<T> extends AbstractMizerValue {
    public ObjectValue(final T value) {
        super(value);
        _parameterizedType = Reflection.getParameterizedTypeForClass(getClass());
        _valueString = String.valueOf(value);
    }

    /**
     * Returns the value of the contained value.
     */
    public T getValue() {
        return convertInstanceOfObject(asObject(), _parameterizedType);
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
        return _valueString;
    }

    /**
     * {@inheritDoc}
     */
    public String on(final Map<Integer, String> map) {
        return _valueString;
    }

    /**
     * {@inheritDoc}
     */
    public void replace(final Variable variable) {
    }

    public static <T> T convertInstanceOfObject(final Object object, final Class<T> clazz) {
        try {
            return clazz.cast(object);
        } catch (ClassCastException e) {
            return null;
        }
    }

    private final Class<T> _parameterizedType;
    private final String   _valueString;
}
