/*
 * org.nrg.config.services.impl.DefaultUserConfigurationService
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 8/26/13 6:15 PM
 */

package org.nrg.config.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.config.services.UserConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class DefaultUserConfigurationService implements UserConfigurationService {

    /**
     * Gets the user configuration
     * @param username    The username of the user associated with the desired configuration.
     * @param configId    The primary configuration identifier.
     * @param keys        Zero to N keys that can be used to drill into the retrieved configuration.
     * @return The content of the requested configuration for the indicated user.
     */
    @Override
    public String getUserConfiguration(final String username, final String configId, String... keys) {
        if (_log.isDebugEnabled()) {
            _log.debug("Retrieving user " + username + " configuration " + configId);
        }
        return _service.getConfig(USER_CONFIG_TOOL, calculateConfigurationPath(username, configId, keys)).getConfigData().getContents();
    }

    /**
     * Sets the user configuration.
     * @param username         The username of the user associated with the desired configuration.
     * @param configId         The primary configuration identifier.
     * @param configuration    The content to set for the requested configuration for the indicated user.
     * @param keys             Zero to N keys that can be used to drill into the retrieved configuration.
     * @throws ConfigServiceException
     */
    @Override
    public void setUserConfiguration(final String username, final String configId, final String configuration, String... keys) throws ConfigServiceException {
        if (_log.isDebugEnabled()) {
            String display;
            if (configuration == null) {
                display = "NULL";
            } else if (configuration.length() < 64) {
                display = configuration;
            } else {
                display = configuration.substring(0, 63) + "...";
            }
            _log.debug("Replacing user " + username + " configuration " + configId + " with contents: " + display);
        }
        _service.replaceConfig(username, "Update", USER_CONFIG_TOOL, calculateConfigurationPath(username, configId, keys), true, configuration);
    }

    private String calculateConfigurationPath(String username, String configId, String... pathElements) {
        return String.format("/%s/%s/%s", username, configId, StringUtils.join(pathElements, '/'));
    }

    private static final String USER_CONFIG_TOOL = "userConfigs";
    private static final Logger _log = LoggerFactory.getLogger(DefaultUserConfigurationService.class);

    @Inject
    private ConfigService _service;
}
