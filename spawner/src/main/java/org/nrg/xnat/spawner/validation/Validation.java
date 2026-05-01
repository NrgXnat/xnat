/*
 * spawner: org.nrg.xnat.spawner.validation.Validation
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.validation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Indicates that a bean is a Spawner validation object.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Validation {
    /**
     * Indicates the validation's ID.
     *
     * @return The ID for the validation.
     */
    String id();

    /**
     * Indicates the validation set to which this validation belongs.
     *
     * @return The validation set for the validation.
     */
    String set();
}
