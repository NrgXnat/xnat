/*
 * DicomEdit: MultipleInitializationException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.mizer.exceptions;

import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.Variable;

/**
 * @author Kevin A. Archie karchie@npg.wustl.edu
 */
public class MultipleInitializationException extends MizerException {
    public MultipleInitializationException(final Variable variable, final Value current, final Value override) {
        super("You attempted to set the value of the variable " + variable.getName() + " to " + override.asString() + " but it has already initialized with the value " + current.asString());
        _variable = variable;
        _current = current;
        _override = override;
    }

    public Variable getVariable() {
        return _variable;
    }

    public Value getCurrentValue() {
        return _current;
    }

    public Value getOverrideValue() {
        return _override;
    }

    private final Variable _variable;
    private final Value    _current;
    private final Value    _override;
}
