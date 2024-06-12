/*
 * framework: org.nrg.framework.beans.XnatPluginBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.beans;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XnatDataModel;
import org.nrg.framework.annotations.XnatPlugin;

import javax.annotation.Nonnull;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.nrg.framework.annotations.XnatPlugin.*;
import static org.nrg.framework.beans.Beans.getXnatDataModelBeansFromProperties;

@SuppressWarnings({"unused", "WeakerAccess"})
@Getter
@Accessors(prefix = "_")
@Slf4j
public class XnatPluginBean {
    @SuppressWarnings("deprecation")
    public XnatPluginBean(final Properties properties) {
        this(properties.getProperty(PLUGIN_CLASS),
             properties.getProperty(PLUGIN_ID),
             properties.getProperty(PLUGIN_NAMESPACE),
             properties.getProperty(PLUGIN_NAME),
             properties.getProperty(PLUGIN_VERSION),
             properties.getProperty(PLUGIN_DESCRIPTION),
             properties.getProperty(PLUGIN_BEAN_NAME),
             properties.getProperty(PLUGIN_ENTITY_PACKAGES),
             properties.getProperty(PLUGIN_LOG_CONFIGURATION, properties.getProperty(PLUGIN_LOG4J_PROPERTIES)),
             getXnatDataModelBeansFromProperties(properties));

        if (properties.containsKey(PLUGIN_LOG4J_PROPERTIES)) {
            log.error("The log4jPropertiesFile setting is deprecated. You should use logConfigurationFile instead. You should also convert any log4j properties files to use the logback XML format. https://logback.qos.ch/translator can help! Your configuration will be added to the logging extensions, but I can't guarantee it will work properly.");
        }
    }

    @SuppressWarnings("deprecation")
    public XnatPluginBean(final TypeElement element, final XnatPlugin plugin) {
        this(element.getQualifiedName().toString(),
             plugin.value(),
             StringUtils.defaultIfBlank(plugin.namespace(), null),
             plugin.name(),
             plugin.version(),
             StringUtils.defaultIfBlank(plugin.description(), null),
             StringUtils.defaultIfBlank(plugin.beanName(), StringUtils.uncapitalize(element.getSimpleName().toString())),
             plugin.entityPackages(),
             StringUtils.defaultIfBlank(plugin.logConfigurationFile(), plugin.log4jPropertiesFile()),
             convertDataModelsToBeans(plugin.dataModels()));
        if (StringUtils.isNotBlank(plugin.log4jPropertiesFile())) {
            log.error("The log4jPropertiesFile setting is deprecated. You should use logConfigurationFile instead. You should also convert any log4j properties files to use the logback XML format. https://logback.qos.ch/translator can help! Your configuration will be added to the logging extensions, but I can't guarantee it will work properly.");
        }
    }

    public XnatPluginBean(final String pluginClass, final String id, final String namespace, final String name, final String version, final String description, final String beanName, final String entityPackages, final String logConfigurationFile, final List<XnatDataModelBean> dataModelBeans) {
        this(pluginClass, id, namespace, name, version, description, beanName, entityPackages != null ? entityPackages.split("\\s*,\\s*") : new String[0], logConfigurationFile, dataModelBeans);
    }

    public XnatPluginBean(final String pluginClass, final String id, final String namespace, final String name, final String version, final String description, final String beanName, final String[] entityPackages, final String logConfigurationFile, final List<XnatDataModelBean> dataModelBeans) {
        this(pluginClass, id, namespace, name, version, description, beanName, entityPackages != null ? Arrays.asList(entityPackages) : Collections.emptyList(), logConfigurationFile, dataModelBeans);
    }

    public XnatPluginBean(final String pluginClass, final String id, final String namespace, final String name, final String version, final String description, final String beanName, final List<String> entityPackages, final String logConfigurationFile, final List<XnatDataModelBean> dataModelBeans) {
        _id                   = id;
        _name                 = name;
        _version              = version;
        _pluginClass          = pluginClass;
        _namespace            = StringUtils.defaultIfBlank(namespace, null);
        _description          = StringUtils.defaultIfBlank(description, null);
        _beanName             = StringUtils.defaultIfBlank(beanName, getBeanName(pluginClass));
        _entityPackages       = entityPackages != null ? ImmutableList.copyOf(entityPackages) : Collections.emptyList();
        _logConfigurationFile = StringUtils.defaultIfBlank(logConfigurationFile, null);
        _dataModelBeans       = ImmutableList.copyOf(dataModelBeans);
        _extendedAttributes   = ArrayListMultimap.create();
    }

    public static List<XnatDataModelBean> convertDataModelsToBeans(final XnatDataModel[] models) {
        return convertDataModelsToBeans(Arrays.asList(models));
    }

    public static List<XnatDataModelBean> convertDataModelsToBeans(final List<XnatDataModel> models) {
        return models.stream().map(XnatDataModelBean::new).collect(Collectors.toList());
    }

    /**
     * Gets all the available extended attributes. Extended attributes aren't set by the plugin directly but can be
     * used by other applications to configure information about a plugin that might be relevant to consumers of the
     * plugin metadata.
     *
     * @return A map containing the available extended attributes.
     */
    public ListMultimap<String, String> getExtendedAttributes() {
        return Multimaps.unmodifiableListMultimap(_extendedAttributes);
    }

    /**
     * Replaces the available extended attributes.
     *
     * @param extendedAttributes The extended attributes to set.
     */
    public void setExtendedAttributes(final ListMultimap<String, String> extendedAttributes) {
        _extendedAttributes.clear();
        _extendedAttributes.putAll(extendedAttributes);
    }

    /**
     * Gets any values set for the specified extended attribute. If the key doesn't exist in the extended attributes,
     * an empty list is returned.
     *
     * @param key The extended attribute to retrieve.
     *
     * @return A list of all values associated with the extended attribute key.
     */
    @Nonnull
    public List<String> getExtendedAttribute(final String key) {
        final List<String> elements = _extendedAttributes.get(key);
        if (elements == null) {
            return Collections.emptyList();
        }
        return ImmutableList.copyOf(elements);
    }

    /**
     * Adds the indicated value to the specified extended attribute. Note that, if one or more values are already set
     * for the attribute, this adds the value to that list.
     *
     * @param key   The extended attribute to set.
     * @param value The value to add to the extended attribute values.
     */
    public void setExtendedAttribute(final String key, final String value) {
        _extendedAttributes.put(key, value);
    }

    /**
     * Adds the indicated values to the specified extended attribute. Note that, if one or more values are already set
     * for the attribute, this adds the values to that list.
     *
     * @param key    The extended attribute to set.
     * @param values The values to add to the extended attribute values.
     */
    public void setExtendedAttribute(final String key, final List<String> values) {
        _extendedAttributes.putAll(key, values);
    }

    /**
     * Puts the indicated value to the specified extended attribute. If any values are set for the attribute, this
     * removes them then sets the new value for the attribute.
     *
     * @param key   The extended attribute to set.
     * @param value The value to set for the extended attribute values.
     */
    public void replaceExtendedAttribute(final String key, final String value) {
        _extendedAttributes.put(key, value);
    }

    /**
     * Puts the indicated values to the specified extended attribute. If any values are set for the attribute, this adds
     * removes them then sets the new values for the attribute.
     *
     * @param key    The extended attribute to set.
     * @param values The values to set for the extended attribute values.
     */
    public void replaceExtendedAttribute(final String key, final List<String> values) {
        _extendedAttributes.putAll(key, values);
    }

    /**
     * Removes the indicated value from the specified extended attribute. If any other values are set for the attribute,
     * they will still be set for the attribute.
     *
     * @param key   The extended attribute to set.
     * @param value The value to remove from the extended attribute values.
     */
    public void removeExtendedAttribute(final String key, final String value) {
        _extendedAttributes.remove(key, value);
    }

    /**
     * Removes the specified extended attribute.
     *
     * @param key The extended attribute to remove.
     */
    public void removeExtendedAttribute(final String key) {
        _extendedAttributes.removeAll(key);
    }

    public Properties asProperties() {
        final Properties properties = new Properties();
        properties.setProperty(PLUGIN_ID, _id);
        properties.setProperty(PLUGIN_BEAN_NAME, _beanName);
        if (StringUtils.isNotBlank(_namespace)) {
            properties.setProperty(PLUGIN_NAMESPACE, _namespace);
        }
        properties.setProperty(PLUGIN_CLASS, _pluginClass);
        properties.setProperty(PLUGIN_NAME, _name);
        properties.setProperty(PLUGIN_DESCRIPTION, _description);
        properties.setProperty(PLUGIN_ENTITY_PACKAGES, String.join(", ", _entityPackages));
        properties.setProperty(PLUGIN_LOG_CONFIGURATION, _logConfigurationFile);
        for (final XnatDataModelBean dataModel : _dataModelBeans) {
            properties.putAll(dataModel.asProperties());
        }
        return properties;
    }

    private String getBeanName(final String config) {
        final int lastToken = config.lastIndexOf(".");
        return StringUtils.uncapitalize(lastToken == -1 ? config : config.substring(lastToken + 1));
    }

    private static List<String> parseCommaSeparatedList(final String entityPackages) {
        if (StringUtils.isBlank(entityPackages)) {
            return Collections.emptyList();
        }
        return Arrays.asList(entityPackages.split("\\s*,\\s*"));
    }

    private final String                            _pluginClass;
    private final String                            _id;
    private final String                            _namespace;
    private final String                            _name;
    private final String                            _version;
    private final String                            _description;
    private final String                            _beanName;
    private final String                            _logConfigurationFile;
    private final List<String>                      _entityPackages;
    private final List<XnatDataModelBean>           _dataModelBeans;
    private final ArrayListMultimap<String, String> _extendedAttributes;
}
