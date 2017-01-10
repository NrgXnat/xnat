/*
 * web: org.nrg.xnat.security.config.AuthenticationProviderConfigurator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
