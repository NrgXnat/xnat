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

import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.prefs.entities.Tool;
import org.nrg.prefs.services.NrgPreferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Properties;
import java.util.Set;

@Primary
@Service
public class PrefsBasedSiteConfigurationService extends PropertiesBasedSiteConfigurationService  {
    @Override
    protected void setPreferenceValue(final String username, final String property, final String value) {
        // TODO: Do we need to add auditing or change logging here?
        if (_log.isDebugEnabled()) {
            _log.debug("User {} is setting the property {} to the value {}", username, property, value);
        }
        try {
            _service.setPreferenceValue(SITE_CONFIG_TOOL_ID, property, value);
        } catch (NrgServiceException e) {
            throw new NrgServiceRuntimeException(e.getServiceError(), e.getMessage(), e);
        }
    }

    @Override
    protected void getPreferenceValuesFromPersistentStore(final Properties properties) {
        try {
            final Set<String> toolIds = _service.getToolIds();
            if (!toolIds.contains(SITE_CONFIG_TOOL_ID)) {
                _log.info("Didn't find a tool for the {} ID, checking for import values from configuration service.", SITE_CONFIG_TOOL_ID);
                final Properties existing = checkForConfigServiceSiteConfiguration();
                if (existing != null) {
                    _log.info("Found {} properties in the configuration service, importing those.", existing.size());
                    properties.putAll(existing);
                }
                final Tool siteConfig = new Tool(SITE_CONFIG_TOOL_ID, "Site Configuration", "This is the main tool for mapping the site configuration", false, null);
                _service.createTool(siteConfig);
            } else {
                _log.info("Working with the existing {} tool, checking for new import values.", SITE_CONFIG_TOOL_ID);

                final Set<String> found = properties.stringPropertyNames();
                final Properties existing = _service.getToolProperties(SITE_CONFIG_TOOL_ID);

                // We're only concerned about properties in found that aren't already in existing. Those should be added to
                // existing. We are NOT concerned about properties in found that have different values from the
                // corresponding properties in existing. This is because the property in existing may have originally had
                // the same value as in found but was then changed in the persistent store. We don't want to overwrite the
                // persisted changes every time we refresh the property store. Therefore we just remove and throw away any
                // properties that have the same name from the found list.
                if (!existing.stringPropertyNames().containsAll(found)) {
                    found.removeAll(existing.stringPropertyNames());
                    _log.info("Found {} new properties, importing into site configuration", found.size());
                    for (final String property : found) {
                        _log.info("Importing new property {}, value: {}", property, properties.getProperty(property));
                        _service.setPreferenceValue(SITE_CONFIG_TOOL_ID, property, properties.getProperty(property));
                    }
                }
                properties.putAll(existing);
            }
        } catch (NrgServiceException e) {
            throw new NrgServiceRuntimeException(e.getServiceError(), e.getMessage(), e);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(PrefsBasedSiteConfigurationService.class);
    private static final String SITE_CONFIG_TOOL_ID = "siteConfig";

    @Inject
    private NrgPreferenceService _service;
}
