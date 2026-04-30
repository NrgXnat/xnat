/*
 * dicom-edit4: org.nrg.dcm.edit.annotations.DE4FAnnotationProcessor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.edit.annotations;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.MetaInfServices;
import org.nrg.framework.processors.NrgAbstractAnnotationProcessor;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DicomEdit4Function annotation processor.
 *
 * This class extends NrgAbstractAnnotationProcessor to create a properties file at
 * META-INF/xnat/dicom-edit4/&lt;function's name&gt;-function.properties
 *
 * The annotation provides strings for the function's name and namespace.
 * The properties file contains the fully qualified name of the class, the function's name, and the function's
 * namespace.
 *
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes("org.nrg.dcm.edit.annotations.DicomEdit4Function")
public class DE4FAnnotationProcessor extends NrgAbstractAnnotationProcessor<DicomEdit4Function> {
    @Override
    protected Map<String, String> processAnnotation(final TypeElement element, final DicomEdit4Function function) {
        final Map<String, String> properties = new LinkedHashMap<>();
        properties.put(DicomEdit4Function.FUNCTION_CLASS, element.getQualifiedName().toString());
        properties.put(DicomEdit4Function.FUNCTION_NAME, function.name());
        final String description = function.description();
        if (StringUtils.isNotBlank(description)) {
            properties.put(DicomEdit4Function.FUNCTION_DESCRIPTION, description);
        }
        return properties;
    }

    @Override
    protected String getPropertiesName(final TypeElement element, final DicomEdit4Function function) {
        return "dicom-edit4/%s-function.properties".formatted(function.name());
    }
}
