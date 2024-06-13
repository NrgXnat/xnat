/*
 * config: org.nrg.config.services.impl.DefaultSiteConfigurationService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.config.services.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

@Service
@Slf4j
public class DefaultSiteConfigurationService extends PropertiesBasedSiteConfigurationService {
    @Autowired
    public DefaultSiteConfigurationService(final ConfigService service, final Environment environment) {
        setEnvironment(environment);
        _service = service;
    }

    @Override
    protected void setPreferenceValue(final String username, final String property, final String value) throws SiteConfigurationException {
        final Properties properties = convertStringToProperties(getPersistedSiteConfiguration().getContents());
        properties.setProperty(property, value);
        setPersistedSiteConfiguration(username, properties, "Setting site configuration property value: " + property);
    }

    @Override
    protected void getPreferenceValuesFromPersistentStore(final Properties properties) throws SiteConfigurationException {
        final Configuration configuration = getPersistedSiteConfiguration();
        if (configuration == null) {
            setPersistedSiteConfiguration(SYSTEM_STARTUP_CONFIG_REFRESH_USER, properties, "Setting site configuration");
        } else {
            final int hash = properties.hashCode();
            properties.putAll(convertStringToProperties(configuration.getContents()));
            if (hash != properties.hashCode()) {
                setPersistedSiteConfiguration(SYSTEM_STARTUP_CONFIG_REFRESH_USER, properties, "Setting site configuration");
            }
        }
    }

    private Configuration getPersistedSiteConfiguration() {
        return _service.getConfig("site", "siteConfiguration");
    }

    private void setPersistedSiteConfiguration(String username, Properties properties, String message) throws SiteConfigurationException {
        log.info("{}, user: {}", message, StringUtils.defaultIfBlank(username, "no details available"));

        try {
            _service.replaceConfig(username, message, "site", "siteConfiguration", convertPropertiesToString(properties, message));
        } catch (ConfigServiceException e) {
            throw new SiteConfigurationException("Error occurred in the configuration service", e);
        }
    }

    private static String convertPropertiesToString(final Properties properties, String message) {
        try (final StringWriter writer = new StringWriter()) {
            properties.store(new PrintWriter(writer), message);
            return writer.getBuffer().toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed convertPropertiesToString", e);
        }
    }

    private static Properties convertStringToProperties(final String contents) {
        try {
            final Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(contents.getBytes()));
            return properties;
        } catch (IOException e) {
            throw new RuntimeException("Failed convertStringToProperties", e);
        }
    }

    private static final String SYSTEM_STARTUP_CONFIG_REFRESH_USER = "admin";

    private final ConfigService _service;
}
