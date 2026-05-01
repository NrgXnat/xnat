/*
 * dicom-edit4: org.nrg.dcm.edit.annotations.DicomEdit4Function
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.edit.annotations;

import org.nrg.dicom.mizer.scripts.ScriptFunction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Used to identify a particular class as a DicomEdit 4 {@link ScriptFunction script function}.
 */
@Target(ElementType.TYPE)
public @interface DicomEdit4Function {
    String FUNCTION_CLASS       = "class";
    String FUNCTION_NAME        = "name";
    String FUNCTION_DESCRIPTION = "description";

    /**
     * Indicates the name of the function. This is the name used when calling the function from a DE4-compliant
     * anonymization script.
     *
     * @return The name of the function.
     */
    String name();

    /**
     * The description of the function.
     *
     * @return The function's description.
     */
    String description() default "";
}
