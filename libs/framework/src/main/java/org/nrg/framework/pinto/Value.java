/*
 * framework: org.nrg.framework.pinto.Value
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.pinto;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RUNTIME)
public @interface Value {
    /**
     * Specifies the short option for this command-line parameter.
     * @return The value of the short option.
     */
    public String value();
}
