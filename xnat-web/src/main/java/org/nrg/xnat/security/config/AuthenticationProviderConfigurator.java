/*
 * org.nrg.xnat.security.config.AuthenticationProviderConfigurator
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.security.config;

import org.springframework.security.authentication.AuthenticationProvider;

import java.util.List;
import java.util.Map;

public interface AuthenticationProviderConfigurator {
    String getConfiguratorId();
    void setConfiguratorId(String configuratorId);
    List<AuthenticationProvider> getAuthenticationProviders(String id, String name);
    List<AuthenticationProvider> getAuthenticationProviders(String id, String name, Map<String, String> properties);
}
