/*
 * framework: org.nrg.framework.beans.XnatPluginBeanManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.beans;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.nrg.framework.annotations.XnatPlugin.PLUGIN_VERSION;

@Service
@Slf4j
public class XnatPluginBeanManager {
    public XnatPluginBeanManager() {
        _pluginBeans = ImmutableMap.copyOf(scanForXnatPluginBeans());
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

    public static Map<String, XnatPluginBean> scanForXnatPluginBeans() {
        final Map<String, XnatPluginBean> pluginBeans = new HashMap<>();
        try {
            for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:META-INF/xnat/**/*-plugin.properties")) {
                try {
                    final Properties properties = PropertiesLoaderUtils.loadProperties(resource);
                    if (!properties.containsKey(PLUGIN_VERSION)) {
                        final String version = getVersionFromResource(resource);
                        properties.setProperty(PLUGIN_VERSION, StringUtils.defaultIfBlank(version, "unknown"));
                    }
                    final XnatPluginBean plugin = new XnatPluginBean(properties);
                    if (log.isDebugEnabled()) {
                        log.debug("Found plugin bean {} in file {}", plugin.getId(), resource.getFilename());
                    }
                    pluginBeans.put(plugin.getId(), plugin);
                } catch (IOException e) {
                    log.error("An error occurred trying to load properties from the resource {}", resource.getFilename(), e);
                }
            }
        } catch (IOException e) {
            log.error("An error occurred trying to locate XNAT plugin definitions. It's likely that none of them were loaded.", e);
        }
        log.debug("Found a total of {} plugin beans", pluginBeans.size());
        return pluginBeans;
    }

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
