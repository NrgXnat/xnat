package org.nrg.dicom.dicomedit.annotation;

import org.kohsuke.MetaInfServices;
import org.nrg.framework.processors.NrgAbstractAnnotationProcessor;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.util.*;

/**
 * DicomEditFunction annotation processor.
 *
 * This class extends NrgAbstractAnnotationProcessor to create a properties file at
 * META-INF/xnat/dicom-edit/&lt;function's name&gt;-function.properties
 *
 * The annotation provides strings for the function's name and namespace.
 * The properties file contains the fully qualified name of the class, the function's name, and the function's
 * namespace.
 *
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes("org.nrg.dicom.dicomedit.annotation.DicomEditFunction")
public class DEFAnnotationProcessor extends NrgAbstractAnnotationProcessor<DicomEditFunction> {
    @Override
    protected Map<String, String> processAnnotation(final TypeElement element, final DicomEditFunction function) {
        final Map<String, String> properties = new LinkedHashMap<>();
        properties.put(DicomEditFunction.FUNCTION_CLASS, element.getQualifiedName().toString());
        properties.put(DicomEditFunction.FUNCTION_NAME, function.name());
        properties.put(DicomEditFunction.FUNCTION_NAMESPACE, function.namespace());
        return properties;
    }

    @Override
    protected String getPropertiesName(final TypeElement element, final DicomEditFunction function) {
        return String.format("dicom-edit/%s-function.properties", function.name());
    }
}
