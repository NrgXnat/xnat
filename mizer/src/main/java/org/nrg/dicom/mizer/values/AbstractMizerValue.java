/*
 * mizer: org.nrg.dicom.mizer.values.AbstractMizerValue
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

import java.util.*;

public abstract class AbstractMizerValue implements Value {
    public static final Value VOID = new AbstractMizerValue("VOID") {
        public SortedSet<Long> getTags() {
            return EMPTY_TAGS;
        }

        public Set<Variable> getVariables() {
            return EMPTY_VARIABLES;
        }

        public void replace(Variable variable) {
            // Nothing to do here.
        }

        public String on(final DicomObjectI dicomObject) throws ScriptEvaluationException {
            return null;
        }

        public String on(Map<Integer, String> m) throws ScriptEvaluationException {
            return null;
        }
    };

    public AbstractMizerValue() {
        this(new Object());
    }

    public AbstractMizerValue(final Object value) {
        _value = value;
    }

    public abstract Set<Variable> getVariables();

    public abstract void replace(final Variable variable);

    public abstract SortedSet<Long> getTags();

    public abstract String on(final DicomObjectI dicomObject) throws ScriptEvaluationException;

    public abstract String on(final Map<Integer, String> m) throws ScriptEvaluationException;

    @Override
    public Object asObject() {
        return _value;
    }

    @Override
    public Boolean asBoolean() {
        return _value != null ? (isBoolean() ? (Boolean) _value : Boolean.valueOf(_value.toString())) : null;
    }

    @Override
    public Integer asInteger() {
        return _value != null ? (isInteger() ? (Integer) _value : Integer.valueOf(_value.toString())) : null;
    }

    @Override
    public Double asDouble() {
        return _value != null ? (isDouble() ? (Double) _value : Double.valueOf(_value.toString())) : null;
    }

    @Override
    public String asString() {
        return _value != null ? (_value instanceof String ? (String) _value : _value.toString()) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Value> asValueList() {
        return isScalar() ? (List<Value>) _value : null;
    }

    @Override
    public boolean isBoolean() {
        return _value != null && _value instanceof Boolean;
    }

    @Override
    public boolean isInteger() {
        return _value != null && _value instanceof Integer;
    }

    @Override
    public boolean isDouble() {
        return _value != null && _value instanceof Double;
    }

    @Override
    public boolean isScalar() {
        return _value != null && _value instanceof List;
    }

    @Override
    public int hashCode() {
        if(_value == null) {
            return 0;
        }

        return _value.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final AbstractMizerValue value = (AbstractMizerValue) object;

        return _value != null ? _value.equals(value._value) : value._value == null;
    }

    @Override
    public String toString() {
        return _value == null ? "null" : _value.toString();
    }

    private final Object _value;
}
