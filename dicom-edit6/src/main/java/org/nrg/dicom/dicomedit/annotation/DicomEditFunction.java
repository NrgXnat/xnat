package org.nrg.dicom.dicomedit.annotation;

import java.lang.annotation.*;

/**
 * Definition of the DicomEditFunction annotation.
 *
 * This annotation marks implementations of AbstractScriptFunction to make them easier to discover on the classpath and
 * instantiate at run time.
 *
 * A properties file will be generated will be created that contains the fully qualified name of the implementing class,
 * the name of the script function, and a namespace to further disambiguate names.
 *
 * @see DEFAnnotationProcessor
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)  // why need this, why not just compile time?
//@Inherited
public @interface DicomEditFunction {
    String FUNCTION_CLASS             = "class";
    String FUNCTION_ID                = "id";
    String FUNCTION_NAMESPACE         = "namespace";
    String FUNCTION_NAME              = "name";
    String FUNCTION_DESCRIPTION       = "description";

    /**
     * In combination with the {@link #namespace()} value, indicates the unique ID for this plugin.
     *
     * @return The ID for the plugin.
     */
//    String value();

    /**
     * In combination with the {@link #name()} value, indicates the unique ID for this plugin.
     *
     * @return The namespace for the plugin.
     */
    String namespace();

    /**
     * The readable name for this plugin.
     *
     * @return The name of the plugin.
     */
    String name();

}
