/*
 * framework: org.nrg.framework.beans.XnatPluginBeanManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.beans;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.nrg.framework.annotations.XnatPlugin.PLUGIN_VERSION;

@Service
@Slf4j
public class XnatPluginBeanManager {
    public static final String                                                    PATTERN_PLUGIN_BEAN_PROPERTIES = "classpath*:META-INF/xnat/**/*-plugin.properties";
    public static final Collector<XnatPluginBean, ?, Map<String, XnatPluginBean>> MAP_COLLECTOR_PLUGIN_BEANS     = Collectors.toMap(XnatPluginBean::getId, Function.identity());

    public XnatPluginBeanManager() {
        this(scanForXnatPluginBeans());
    }

    public XnatPluginBeanManager(final List<XnatPluginBean> pluginBeans) {
        this(pluginBeans.stream().collect(MAP_COLLECTOR_PLUGIN_BEANS));
    }

    public XnatPluginBeanManager(final Map<String, XnatPluginBean> pluginBeans) {
        _pluginBeans = pluginBeans;
    }

    /**
     * Scans for plugin bean definitions in property files matching the {@link #PATTERN_PLUGIN_BEAN_PROPERTIES} pattern.
     * All matching property files are converted to resources and passed to {@link #scanForXnatPluginBeans(List)}, which
     * actually loads and parses the property files.
     *
     * @return A map of plugin beans keyed on the plugin ID.
     */
    public static Map<String, XnatPluginBean> scanForXnatPluginBeans() {
        try {
            return scanForXnatPluginBeans(BasicXnatResourceLocator.getResources(PATTERN_PLUGIN_BEAN_PROPERTIES));
        } catch (IOException e) {
            log.error("An error occurred trying to locate XNAT plugin definitions. It's likely that none of them were loaded.", e);
            return Collections.emptyMap();
        }
    }

    /**
     * Scans for plugin bean definitions in property files loaded from the incoming resource references. Each property file is
     * passed to {@link XnatPluginBean#XnatPluginBean(Properties)} to create the actual plugin bean.
     *
     * @param resources A list of resources containing property file definitions.
     *
     * @return A map of plugin beans keyed on the plugin ID.
     */
    public static Map<String, XnatPluginBean> scanForXnatPluginBeans(final List<Resource> resources) {
        return resources.stream().map(RESOURCE_TO_PROPERTIES).filter(Objects::nonNull).map(XnatPluginBean::new).collect(MAP_COLLECTOR_PLUGIN_BEANS);
    }

    public Set<String> getPluginIds() {
        return _pluginBeans.keySet();
    }

    public XnatPluginBean getPlugin(final String pluginId) {
        return _pluginBeans.get(pluginId);
    }

    public Map<String, XnatPluginBean> getPluginBeans() {
        return _pluginBeans;
    }

    private static final Function<Resource, Properties> RESOURCE_TO_PROPERTIES = resource -> {
        try {
            final Properties properties = PropertiesLoaderUtils.loadProperties(resource);
            if (!properties.containsKey(PLUGIN_VERSION)) {
                final String version = getVersionFromResource(resource);
                properties.setProperty(PLUGIN_VERSION, StringUtils.defaultIfBlank(version, "unknown"));
            }
            return properties;
        } catch (IOException e) {
            log.error("An error occurred trying to load properties from the resource {}", resource.getFilename(), e);
            return null;
        }
    };

    private static String getVersionFromResource(final Resource resource) throws IOException {
        final Matcher matcher = EXTRACT_PLUGIN_VERSION.matcher(resource.getURI().toString());
        if (matcher.find()) {
            return matcher.group("version");
        }
        return null;
    }

    private static final Pattern EXTRACT_PLUGIN_VERSION = Pattern.compile("^.*/[A-Za-z0-9._-]+-(?<version>\\d.*)\\.jar.*$");

    private final Map<String, XnatPluginBean> _pluginBeans;
}
