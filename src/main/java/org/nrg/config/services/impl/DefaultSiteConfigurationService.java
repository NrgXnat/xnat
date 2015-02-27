/**
 * SiteConfiguration
 * (C) 2012 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 9/18/12 by rherri01
 */
package org.nrg.config.services.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.config.services.ConfigService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

@Service
public class DefaultSiteConfigurationService extends PropertiesBasedSiteConfigurationService {

    @Override
    protected void setPreferenceValue(final String username, final String property, final String value) throws SiteConfigurationException {
        Properties properties = convertStringToProperties(getPersistedSiteConfiguration().getContents());
        properties.setProperty(property, value);
        setPersistedSiteConfiguration(username, properties, "Setting site configuration property value: " + property);
    }

    @Override
    protected void getPreferenceValuesFromPersistentStore(final Properties properties) throws SiteConfigurationException {
        Configuration configuration = getPersistedSiteConfiguration();
        if (configuration == null) {
            setPersistedSiteConfiguration(SYSTEM_STARTUP_CONFIG_REFRESH_USER, properties, "Setting site configuration");
        } else {
            int hash = properties.hashCode();
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
        if (_log.isInfoEnabled()) {
            if (StringUtils.isBlank(username)) {
                _log.info(message + ", no user details available");
            } else {
                _log.info(message + ", user: " + username);
            }
        }

        try {
            _service.replaceConfig(username, message, "site", "siteConfiguration", convertPropertiesToString(properties, message));
        } catch (ConfigServiceException e) {
            throw new SiteConfigurationException("Error occurred in the configuration service", e);
        }
    }

    private static String convertPropertiesToString(final Properties properties, String message) {
        StringWriter writer = new StringWriter();
        try {
            properties.store(new PrintWriter(writer), message);
        } catch (IOException e) {
            throw new RuntimeException("Failed convertPropertiesToString", e);
        }
        return writer.getBuffer().toString();
    }

    private static Properties convertStringToProperties(final String contents) {
        Properties properties = new Properties();
        try {
            properties.load(new ByteArrayInputStream(contents.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException("Failed convertStringToProperties", e);
        }
        return properties;
    }

    private static final String SYSTEM_STARTUP_CONFIG_REFRESH_USER = "admin";
    private static final Log _log = LogFactory.getLog(DefaultSiteConfigurationService.class);

    @Inject
    private ConfigService _service;
}
