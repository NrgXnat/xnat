/*
 * web: org.nrg.xnat.security.alias.ClearExpiredAliasTokens
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.alias;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xnat.task.AbstractXnatRunnable;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Getter(PRIVATE)
@Setter(PRIVATE)
@Accessors(prefix = "_")
public class ClearExpiredAliasTokens extends AbstractXnatRunnable {
    public ClearExpiredAliasTokens(final AliasTokenService aliasTokenService, final SiteConfigPreferences preferences) {
        _service = aliasTokenService;
        _preferences = preferences;
    }

    /**
     * Executes the alias token sweep function.
     */
    @Override
    protected void runTask() {
        String timeout = getPreferences().getAliasTokenTimeout();
        log.debug("Executing alias token sweep function with timeout value {}", timeout);
        getService().invalidateExpiredTokens(timeout);
    }

    private final AliasTokenService _service;
    private final SiteConfigPreferences _preferences;
}
