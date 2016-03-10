package org.nrg.prefs.processors;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.MetaInfServices;
import org.nrg.framework.exceptions.NrgRuntimeException;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.processors.NrgAbstractAnnotationProcessor;
import org.nrg.prefs.annotations.NrgPreferencesBean;
import org.springframework.context.MessageSource;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Processes the {@link NrgPreferencesBean} annotation and generates a properties file that is used by XNAT to locate the
 * prefs tool on application start-up.
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes("org.nrg.prefs.annotations.NrgPreferencesBean")
public class NrgPreferencesBeanAnnotationProcessor extends NrgAbstractAnnotationProcessor<NrgPreferencesBean> {
    /**
     * This processes the annotation of the parameterized type on the specified element, which should be an instantiable
     * class (i.e. not an interface or abstract class). The annotation is processed according to the logic provided in
     * the implementation of this method and converted into a properties object.
     *
     * @param element    The annotated class element.
     * @param bean The annotation.
     * @return The attributes for the annotation converted into a properties object.
     */
    @Override
    protected Properties processAnnotation(final TypeElement element, final NrgPreferencesBean bean) {
        final Properties properties = new Properties();
        properties.setProperty("toolClass", element.toString());
        properties.setProperty("toolId", bean.toolId());
        try {
            properties.setProperty("toolName", getMessage(bean.toolNameKey(), bean.toolName()));
        } catch (NrgServiceException e) {
            throw new NrgRuntimeException("You must specify at least one of the toolName or toolNameKey attributes for the PreferencesBean annotation on the " + element.toString() + " class.");
        }
        try {
            properties.setProperty("description", getMessage(bean.descriptionKey(), bean.description()));
        } catch (NrgServiceException ignored) {
            // Just ignore this: the description isn't necessary.
        }
        final List<String> transformers = getTypeElementValues(element, "transformers");
        if (transformers != null) {
            properties.setProperty("transformers", Joiner.on(",").join(transformers));
        }
        final List<String> resolvers = getTypeElementValues(element, "resolver");
        if (resolvers != null && resolvers.size() > 0) {
            if (resolvers.size() > 1) {
                throw new RuntimeException("You can only specify zero or one resolvers for the NrgPreferencesBean on the " + element.toString() + " class.");
            }
            properties.setProperty("resolver", resolvers.get(0));
        }
        return properties;
    }

    private String getMessage(final String key, final String literal) throws NrgServiceException {
        if (StringUtils.isBlank(key) && StringUtils.isBlank(literal)) {
            throw new NrgServiceException(NrgServiceError.ConfigurationError);
        }
        final String message = StringUtils.isNotBlank(key) ? _messageSource.getMessage(key, null, Locale.getDefault()) : null;
        return StringUtils.isNotBlank(message) ? message : literal;
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
    protected String getPropertiesName(final NrgPreferencesBean annotation) {
        return annotation.toolId() + "-preferences";
    }

    @Inject
    private MessageSource _messageSource;
}
