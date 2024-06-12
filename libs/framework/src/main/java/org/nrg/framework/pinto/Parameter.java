/*
 * framework: org.nrg.framework.pinto.Parameter
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
public @interface Parameter {
    /**
     * Specifies the short option for this command-line parameter.
     * @return The value of the short option.
     */
    String value();
    /**
     * Specifies the long option for this command-line parameter.
     * @return The value of the long option.
     */
    String longOption() default "";

    /**
     * Sets the help text to display for the parameter.
     * @return The help text.
     */
    String help() default "";

    /**
     * Indicates whether this command-line parameter is required to be specified. By default, parameters are not
     * required.
     * @return Returns true if the parameter is required, false otherwise.
     */
    boolean required() default false;

    /**
     * Provides the default value for this parameter if not specified.
     * @return A string representation of the default value if specified. Empty string indicates no default.
     */
    String defaultValue() default "";

    /**
     * The number of accepted arguments for this parameter. This is specified using the {@link ArgCount} enumeration. In
     * the case of {@link ArgCount#SpecificCount}, you can set the specific count to be used with the {@link
     * #exactArgCount()} attribute. The default value for this is {@link ArgCount#OneArgument}.
     * @return The number of accepted arguments for this parameter.
     */
    ArgCount argCount() default ArgCount.OneArgument;

    /**
     * Indicates whether multiple instances of this parameter are allowed. This defaults to false.
     * @return Whether this parameter can be specified multiple times on the command line.
     */
    boolean multiplesAllowed() default false;

    /**
     * Whe the {@link #argCount()} attribute is set to {@link ArgCount#SpecificCount}, this attribute
     * specifies the value for the specific number of arguments for this parameter.
     * @return The specific number of arguments for this parameter.
     */
    int exactArgCount() default 0;

    /**
     * The type into which the parameter arguments should be coerced.
     * @return The type into which the parameter arguments should be coerced.
     */
    Class<?> type() default boolean.class;
}
