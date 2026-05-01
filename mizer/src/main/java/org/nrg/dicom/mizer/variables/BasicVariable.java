/*
 * mizer: org.nrg.dicom.mizer.variables.TestVariable
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.variables;

import org.nrg.dicom.mizer.exceptions.MultipleInitializationException;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;

public class BasicVariable implements Variable {
    /**
     * Creates the variable with the indicated name.
     *
     * @param name The name of the variable to create.
     */
    public BasicVariable(final String name) {
        this(name, null, null);
    }

    /**
     * Creates the variable with the indicated name and export field.
     *
     * @param name  The name of the variable to create.
     * @param value The value to set for the variable to create.
     */
    public BasicVariable(final String name, final String value) {
        this(name, new ConstantValue(value), null);
    }

    /**
     * Creates the variable with the indicated name and initial value.
     *
     * @param name  The name of the variable to create.
     * @param value The initial value of the variable to create.
     */
    public BasicVariable(final String name, final Value value) {
        this(name, value, value);
    }

    /**
     * Creates the variable with the indicated name, export field, initial value, and value.
     *
     * @param name         The name of the variable to create.
     * @param value        The value to set for the variable to create.
     * @param initialValue The initial value of the variable to create.
     */
    public BasicVariable(final String name, final Value value, final Value initialValue) {
        _name = name;
        _value = value;
        _initialValue = initialValue == null ? value : initialValue;
    }

    /**
     * Creates a "shallow" copy of the submitted variable object. This will copy all variable properties available
     * through the standard property assessors, but may miss significant state properties from particular
     * implementations.
     *
     * @param variable The variable to be copied.
     */
    public BasicVariable(final Variable variable) {
        _name = variable.getName();
        _value = variable.getValue();
        _initialValue = variable.getInitialValue();
        _description = variable.getDescription();
        _exportField = variable.getExportField();
        _hidden = variable.isHidden();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return _name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return _description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDescription(final String description) {
        _description = description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value getValue() {
        return _value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String setValue(final String value) {
        final String current = _value == null ? null : _value.asString();
        _value = new ConstantValue(value);
        return current;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(final Value value) {
        _value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value getInitialValue() {
        return _initialValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInitialValue(final String initialValue) throws MultipleInitializationException {
        setInitialValue(new ConstantValue(initialValue));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInitialValue(final Value initialValue) throws MultipleInitializationException {
        if (_initialValue != null) {
            throw new MultipleInitializationException(this, _initialValue, initialValue);
        }
        _initialValue = initialValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getExportField() {
        return _exportField;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExportField(final String exportField) {
        _exportField = exportField;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHidden() {
        return _hidden;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIsHidden(final boolean hidden) {
        _hidden = hidden;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof Variable && getName().equals(((Variable) object).getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return _name + " = " + _value + (_initialValue != null ? " (initial value = " + _initialValue.asString() + ")" : "");
    }

    private final String  _name;
    private       String  _description;
    private       String  _exportField;
    private       Value   _value;
    private       Value   _initialValue;
    private       boolean _hidden;
}
