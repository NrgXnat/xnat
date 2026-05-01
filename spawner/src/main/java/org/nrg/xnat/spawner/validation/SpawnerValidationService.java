/*
 * spawner: org.nrg.xnat.spawner.validation.SpawnerValidationService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.validation;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xnat.spawner.exceptions.UnknownValidationSetException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SpawnerValidationService {
    public static final String STANDARD_SET = "standard";

    /**
     * Creates the Spawner validation service with the list of validators.
     *
     * @param validators The list of validators to reference.
     */
    @Autowired
    public SpawnerValidationService(final List<Validator> validators) {
        for (final Validator validator : validators) {
            final Validation validation = validator.getClass().getAnnotation(Validation.class);
            final String     set        = validation.set();
            final String     id         = validation.id();

            final Map<String, Validator> validatorSet;
            if (!_validations.containsKey(set)) {
                validatorSet = new HashMap<>();
                _validations.put(set, validatorSet);
            } else {
                validatorSet = _validations.get(set);
            }

            if (validatorSet.containsKey(id)) {
                if (validatorSet.get(id).getClass().equals(validator.getClass())) {
                    log.warn("Found duplicate instances of a validator with the ID " + id + ". These are the same class, so execution can proceed, but you may have an issue with duplicate instantiations of this validator.");
                } else {
                    throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "Found duplicate instances of a validator with the ID " + id + " with different classes: " + validator.getClass() + " and " + validatorSet.get(id).getClass());
                }
            } else {
                validatorSet.put(id, validator);
            }
        }
    }

    /**
     * Gets all validations organized by set.
     *
     * @return A map of all validations organized by set.
     */
    public Map<String, List<String>> getValidations() {
        return new HashMap<String, List<String>>() {{
            for (final String set : _validations.keySet()) {
                put(set, new ArrayList<>(getValidationSet(set).keySet()));
            }
        }};
    }

    /**
     * Gets the indicated validation set.
     *
     * @param set The ID of the validator set.
     *
     * @return The indicated validation set.
     */
    public Map<String, Validator> getValidationSet(final String set) {
        return new HashMap<String, Validator>() {{
            putAll(_validations.get(set));
        }};
    }

    /**
     * Validates the submitted value with the indicated {@link Validator validator}. This specifies validators from the
     * default provided validator set.
     *
     * @param id    The ID of the validator from the default set.
     * @param value The value to validate.
     *
     * @return A {@link Validated} object indicating the result of the validation operation.
     *
     * @throws UnknownValidationSetException When the specified validator ID isn't found.
     */
    public Validated validate(final String id, final String value) throws UnknownValidationSetException {
        if (id.contains(":")) {
            final String[] atoms = id.split(":", 2);
            return validate(atoms[0], atoms[1], value);
        }
        return validate(STANDARD_SET, id, value);
    }

    /**
     * Validates the submitted value with the indicated {@link Validator validator}. This specifies validators from the
     * validator set indicated by the set parameter.
     *
     * @param set   The ID of the validator set.
     * @param id    The ID of the validator from the specified validator set.
     * @param value The value to validate.
     *
     * @return A {@link Validated} object indicating the result of the validation operation.
     *
     * @throws UnknownValidationSetException When the specified validator set or ID isn't found.
     */
    public Validated validate(final String set, final String id, final String value) throws UnknownValidationSetException {
        if (!_validations.containsKey(set)) {
            throw new UnknownValidationSetException(set, id);
        }
        final Map<String, Validator> validatorSet = _validations.get(set);
        if (!validatorSet.containsKey(id)) {
            throw new UnknownValidationSetException(set, id);
        }
        final Validator validator = validatorSet.get(id);
        return validator.validate(value);
    }

    private final Map<String, Map<String, Validator>> _validations = new HashMap<>();
}
