/*
 * framework: org.nrg.framework.processors.XnatPluginAnnotationProcessor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.processors;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.MetaInfServices;
import org.nrg.framework.annotations.XnatDataModel;
import org.nrg.framework.annotations.XnatPlugin;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.nrg.framework.annotations.XnatPlugin.*;

/**
 * Processes the {@link XnatPlugin} annotation and generates the plugin's properties file that used by XNAT for plugin
 * discovery. The basis for this code was adapted from <a href="http://kohsuke.org">Kohsuke Kawaguchi's</a> code for the
 * <a href="http://metainf-services.kohsuke.org">META-INF/services generator</a>. This does the same basic thing but
 * generates a file named "META-INF/xnat/id-plugin.properties", where the <i>id</i> is taken from the value set for the
 * {@link XnatPlugin#value()} attribute on the annotation.
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes("org.nrg.framework.annotations.XnatPlugin")
@Slf4j
public class XnatPluginAnnotationProcessor extends NrgAbstractAnnotationProcessor<XnatPlugin> {
    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> processAnnotation(final TypeElement element, final XnatPlugin plugin) {
        final Map<String, String> properties = new LinkedHashMap<>();
        properties.put(PLUGIN_ID, plugin.value());
        properties.put(PLUGIN_CLASS, element.getQualifiedName().toString());
        properties.put(PLUGIN_NAME, plugin.name());
        if (StringUtils.isNotBlank(plugin.version())) {
            properties.put(PLUGIN_VERSION, plugin.version());
        }
        if (StringUtils.isNotBlank(plugin.namespace())) {
            properties.put(PLUGIN_NAMESPACE, plugin.namespace());
        }
        if (StringUtils.isNotBlank(plugin.description())) {
            properties.put(PLUGIN_DESCRIPTION, plugin.description());
        }

        final String beanName;
        if (StringUtils.isNotBlank(plugin.beanName())) {
            beanName = plugin.beanName();
        } else {
            beanName = StringUtils.uncapitalize(element.getSimpleName().toString());
        }
        properties.put(PLUGIN_BEAN_NAME, beanName);
        final List<String> entityPackages = Arrays.asList(plugin.entityPackages());
        if (entityPackages.size() > 0) {
            properties.put(PLUGIN_ENTITY_PACKAGES, Joiner.on(", ").join(entityPackages));
        }

        if (StringUtils.isNotBlank(plugin.logConfigurationFile())) {
            properties.put(PLUGIN_LOG_CONFIGURATION, plugin.logConfigurationFile());
        } else {
            //noinspection deprecation
            if (StringUtils.isNotBlank(plugin.log4jPropertiesFile())) {
                log.error("The log4jPropertiesFile setting is deprecated. You should use logConfigurationFile instead. You should also convert any log4j properties files to use the logback XML format. https://logback.qos.ch/translator can help! Your configuration will be added to the logging extensions, but I can't guarantee it will work properly.");
                //noinspection deprecation
                properties.put(PLUGIN_LOG_CONFIGURATION, plugin.log4jPropertiesFile());
            }
        }

        final List<String> openUrls = Arrays.asList(plugin.openUrls());
        if (!openUrls.isEmpty()) {
            properties.put(PLUGIN_OPEN_URLS, Joiner.on(", ").join(openUrls));
        }

        final List<String> adminUrls = Arrays.asList(plugin.adminUrls());
        if (!adminUrls.isEmpty()) {
            properties.put(PLUGIN_ADMIN_URLS, Joiner.on(", ").join(adminUrls));
        }

        for (final XnatDataModel dataModel : plugin.dataModels()) {
            final String elementPrefix = getElementPrefix(dataModel);
            properties.put(elementPrefix + "secured", Boolean.toString(dataModel.secured()));
            final String singular = dataModel.singular();
            if (StringUtils.isNotBlank(singular)) {
                properties.put(elementPrefix + "singular", singular);
            }
            final String plural = dataModel.plural();
            if (StringUtils.isNotBlank(plural)) {
                properties.put(elementPrefix + "plural", plural);
            }
            final String code = dataModel.code();
            if (StringUtils.isNotBlank(code)) {
                properties.put(elementPrefix + "code", code);
            }
        }

        return properties;
    }

    /**
     * Gets the element prefix.
     *
     * @param dataModel the data model
     *
     * @return the element prefix
     */
    private String getElementPrefix(final XnatDataModel dataModel) {
        return "dataModel." + dataModel.value().replace(":", ".") + ".";
    }


    /* (non-Javadoc)
     * @see org.nrg.framework.processors.NrgAbstractAnnotationProcessor#getPropertiesName(javax.lang.model.element.TypeElement, java.lang.annotation.Annotation)
     */
    @Override
    protected String getPropertiesName(final TypeElement element, final XnatPlugin plugin) {
        final String namespace = plugin.namespace();
        final String pluginId  = plugin.value();
        if (StringUtils.isBlank(namespace)) {
            return String.format("META-INF/xnat/%s-plugin.properties", pluginId);
        }
        return String.format("META-INF/xnat/%s/%s-plugin.properties", namespace, pluginId);
    }
}
