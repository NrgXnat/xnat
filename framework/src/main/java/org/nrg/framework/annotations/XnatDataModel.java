/*
 * framework: org.nrg.framework.annotations.XnatDataModel
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.annotations;

import java.lang.annotation.*;

/**
 * Informs XFT of data types to be loaded.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface XnatDataModel {
    String DATA_MODEL_TYPE     = "type";
    String DATA_MODEL_SECURED  = "secured";
    String DATA_MODEL_SINGULAR = "singular";
    String DATA_MODEL_PLURAL   = "plural";
    String DATA_MODEL_CODE     = "code";

    /**
     * Provides the fully qualified name of the data model element. This includes the namespace, e.g. foo:barData.
     *
     * @return The name of the element.
     */
    String value();

    /**
     * Indicates whether the data model element is secured by default. This defaults to true.
     *
     * @return Whether the data model element is secured by default.
     */
    boolean secured() default true;

    /**
     * Defines the default singular name for the data model element.
     *
     * @return The default singular name.
     */
    String singular() default "";

    /**
     * Defines the default plural name for the data model element.
     *
     * @return The default plural name.
     */
    String plural() default "";

    /**
     * Defines the default code for the data model element.
     *
     * @return The default code.
     */
    String code() default "";
}
