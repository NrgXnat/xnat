/*
 * config: org.nrg.config.services.impl.DefaultUserConfigurationService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.config.services.UserConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultUserConfigurationService implements UserConfigurationService {
    @Autowired
    public DefaultUserConfigurationService(final ConfigService service) {
        _service = service;
    }

    /**
     * Gets the user configuration
     *
     * @param username The username of the user associated with the desired configuration.
     * @param configId The primary configuration identifier.
     * @param keys     Zero to N keys that can be used to drill into the retrieved configuration.
     *
     * @return The content of the requested configuration for the indicated user.
     */
    @Override
    public String getUserConfiguration(final String username, final String configId, String... keys) {
        log.debug("Retrieving user {} configuration {}", username, configId);
        return _service.getConfig(USER_CONFIG_TOOL, calculateConfigurationPath(username, configId, keys)).getConfigData().getContents();
    }

    /**
     * Sets the user configuration.
     *
     * @param username      The username of the user associated with the desired configuration.
     * @param configId      The primary configuration identifier.
     * @param configuration The content to set for the requested configuration for the indicated user.
     * @param keys          Zero to N keys that can be used to drill into the retrieved configuration.
     *
     * @throws ConfigServiceException When an error occurs accessing or updating the configuration service.
     */
    @Override
    public void setUserConfiguration(final String username, final String configId, final String configuration, String... keys) throws ConfigServiceException {
        log.debug("Replacing user {} configuration {} with contents: {}", username, configId, trimForDisplay(configuration));
        _service.replaceConfig(username, "Update", USER_CONFIG_TOOL, calculateConfigurationPath(username, configId, keys), true, configuration);
    }

    private String trimForDisplay(final String display) {
        if (StringUtils.isBlank(display)) {
            return NULL_DISPLAY;
        }
        if (StringUtils.length(display) < 64) {
            return display;
        }
        return StringUtils.truncate(display, 63) + "...";
    }

    private String calculateConfigurationPath(String username, String configId, String... pathElements) {
        return String.format("/%s/%s/%s", username, configId, StringUtils.join(pathElements, '/'));
    }

    private static final String NULL_DISPLAY     = "NULL";
    private static final String USER_CONFIG_TOOL = "userConfigs";

    private final ConfigService _service;
}
