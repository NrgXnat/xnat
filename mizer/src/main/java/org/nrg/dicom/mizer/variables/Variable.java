/*
 * DicomEdit: Variable
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.variables;

import org.nrg.dicom.mizer.exceptions.MultipleInitializationException;
import org.nrg.dicom.mizer.values.Value;

/**
 * Defines the interface for any variable used in Mizer operations.
 */
public interface Variable {
    /**
     * Gets the (brief) name of this variable.
     *
     * @return The name of the variable.
     */
    String getName();

    /**
     * Gets the human-readable description of this variable.
     *
     * @return The description of this variable.
     */
    String getDescription();

    /**
     * Sets the human-readable description of this variable.
     *
     * @param description The description to set.
     */
    void setDescription(final String description);

    /**
     * Gets the current concrete value for this variable. This returns null if no value has been set.
     *
     * @return The current value of the variable.
     */
    Value getValue();

    /**
     * Sets the current concrete value of this variable.
     *
     * @param value The value to set.
     */
    String setValue(final String value);

    /**
     * Sets the current concrete value of this variable.
     *
     * @param value The value to set.
     */
    void setValue(final Value value);

    /**
     * Gets the initial value assigned to this variable. This returns null if no initial value was set.
     *
     * @return The initial value for this variable.
     */
    Value getInitialValue();

    /**
     * Sets an initial value for this variable.
     *
     * @param initialValue The value to set.
     *
     * @throws MultipleInitializationException if an initial value has already been assigned.
     */
    void setInitialValue(final String initialValue) throws MultipleInitializationException;

    /**
     * Sets an initial value for this variable.
     *
     * @param initialValue The value to set.
     *
     * @throws MultipleInitializationException if an initial value has already been assigned.
     */
    void setInitialValue(final Value initialValue) throws MultipleInitializationException;

    /**
     * Gets the label that will be used for exporting this variable.
     *
     * @return export field label, or null if not set
     */
    String getExportField();

    /**
     * Sets the label to be used for exporting this variable.
     *
     * @param field The export label.
     */
    void setExportField(final String field);

    /**
     * Should this variable be hidden in a user interface?
     *
     * @return true if this variable should be hidden from users
     */
    boolean isHidden();

    /**
     * Specifies whether this variable should be hidden in a user interface.
     *
     * @param isHidden Whether the variable is hidden.
     */
    void setIsHidden(final boolean isHidden);
}
