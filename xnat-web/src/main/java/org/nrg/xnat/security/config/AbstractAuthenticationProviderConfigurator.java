/*
 * org.nrg.xnat.security.config.AbstractAuthenticationProviderConfigurator
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.security.config;

public abstract class AbstractAuthenticationProviderConfigurator implements AuthenticationProviderConfigurator {
    private String _configuratorId;

    @Override
    public String getConfiguratorId() {
        return _configuratorId;
    }

    @Override
    public void setConfiguratorId(String configuratorId) {
        _configuratorId = configuratorId;
    }
}
