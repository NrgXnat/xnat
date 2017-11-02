/*
 * web: org.nrg.xnat.security.alias.ClearExpiredAliasTokens
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.alias;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.services.AliasTokenService;

@Slf4j
public class ClearExpiredAliasTokens implements Runnable {
    public ClearExpiredAliasTokens(final AliasTokenService aliasTokenService, final SiteConfigPreferences preferences) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing the alias token sweeper job");
        }
        _service = aliasTokenService;
        _preferences = preferences;
    }

    /**
     */
    @Override
    public void run() {
        final String aliasTokenTimeout = _preferences.getAliasTokenTimeout();
        if (log.isDebugEnabled()) {
            log.debug("Executing alias token sweep function to remove alias tokens expired based on preference setting {}", aliasTokenTimeout);
        }
        _service.invalidateExpiredTokens(aliasTokenTimeout);
    }

    private final SiteConfigPreferences _preferences;
    private final AliasTokenService     _service;
}
