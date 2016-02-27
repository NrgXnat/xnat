package org.nrg.prefs.processors;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.MetaInfServices;
import org.nrg.framework.processors.NrgAbstractAnnotationProcessor;
import org.nrg.prefs.annotations.NrgPrefValue;
import org.nrg.prefs.annotations.NrgPrefsTool;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.util.Properties;
import java.util.Set;

/**
 * Processes the {@link NrgPrefsTool} annotation and generates a properties file that is used by XNAT to locate the
 * prefs tool on application start-up.
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes("org.nrg.prefs.annotations.NrgPrefsTool")
public class NrgPrefsToolAnnotationProcessor extends NrgAbstractAnnotationProcessor<NrgPrefsTool> {
    /**
     * This processes the annotation of the parameterized type on the specified element, which should be an instantiable
     * class (i.e. not an interface or abstract class). The annotation is processed according to the logic provided in
     * the implementation of this method and converted into a properties object.
     *
     * @param element    The annotated class element.
     * @param prefsTool The annotation.
     * @return The attributes for the annotation converted into a properties object.
     */
    @Override
    protected Properties processAnnotation(final TypeElement element, final NrgPrefsTool prefsTool) {
        final Properties properties = new Properties();
        properties.setProperty("toolId", prefsTool.toolId());
        prefsTool.toolName();
        if (StringUtils.isNotEmpty(prefsTool.description())) {
            properties.setProperty("description", prefsTool.description());
        }
        if (prefsTool.preferences() != null) {
            for (final NrgPrefValue value : prefsTool.preferences()) {
                final String valueType = getValueType(value);
                properties.setProperty("preferences." + value.name(), value.defaultValue() + (!StringUtils.equals(String.class.getName(), valueType) ? " [" + valueType + "]" : ""));
            }
        }
        if (prefsTool.strict()) {
            properties.setProperty("strict", Boolean.TRUE.toString());
        }
        final String preferencesClass = getTypeElementValue(element, "preferencesClass");
        if (StringUtils.isNotEmpty(preferencesClass)) {
            properties.setProperty("preferencesClass", preferencesClass);
        }
        if (StringUtils.isNotEmpty(prefsTool.resolverId())) {
            properties.setProperty("resolverId", prefsTool.resolverId());
        }
        if (!prefsTool.property().equals("preferences")) {
            properties.setProperty("property", prefsTool.property());
        }
        return properties;
    }

    private String getValueType(final NrgPrefValue value) {
        try {
            value.valueType();
        } catch (MirroredTypeException exception) {
            return exception.getTypeMirror().toString();
        }
        return null;
    }

    private String getPreferences(final NrgPrefValue value) {
        try {
            value.valueType();
        } catch (MirroredTypeException exception) {
            return exception.getTypeMirror().toString();
        }
        return null;
    }

    /**
     * Returns the name for the properties resource to be generated for the annotation instance. This can be as simple
     * as just the bare name of the properties bundle without a path or properties extension: these will be added if not
     * present.
     *
     * @param annotation The annotation instance.
     * @return The name for the properties resource.
     */
    @Override
    protected String getPropertiesName(final NrgPrefsTool annotation) {
        return annotation.toolId() + "-preferences";
    }
}
