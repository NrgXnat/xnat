/*
 * config: org.nrg.config.services.UserConfigurationService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config.services;

import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.services.NrgService;

public interface UserConfigurationService extends NrgService {
    /**
     * Gets the user configuration
     *
     * @param username The username of the user associated with the desired configuration.
     * @param configId The primary configuration identifier.
     * @param keys     Zero to N keys that can be used to drill into the retrieved configuration.
     * @return The content of the requested configuration for the indicated user.
     */
    String getUserConfiguration(String username, String configId, String... keys);

    /**
     * Sets the user configuration.
     *
     * @param username      The username of the user associated with the desired configuration.
     * @param configId      The primary configuration identifier.
     * @param configuration The content to set for the requested configuration for the indicated user.
     * @param keys          Zero to N keys that can be used to drill into the retrieved configuration.
     * @throws ConfigServiceException When an error occurs accessing or updating the configuration service.
     */
    void setUserConfiguration(String username, String configId, String configuration, String... keys) throws ConfigServiceException;
}
