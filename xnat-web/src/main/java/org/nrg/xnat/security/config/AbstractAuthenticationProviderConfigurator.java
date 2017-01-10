/*
 * web: org.nrg.xnat.security.config.AbstractAuthenticationProviderConfigurator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
