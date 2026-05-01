/*
 * spawner: org.nrg.xnat.spawner.validation.Validator
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.validation;

/**
 * Defines the implementation for Spawner validators.
 */
public interface Validator {
    /**
     * Validates the submitted value according to the validator's specific logic. The results of the validation
     * operation are contained in the returned {@link Validated} object.
     *
     * @param value    The value to be validated.
     *
     * @return The {@link Validated validation results}.
     */
    Validated validate(final String value);
}
