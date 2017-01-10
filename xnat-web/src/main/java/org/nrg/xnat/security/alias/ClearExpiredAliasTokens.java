/*
 * web: org.nrg.xnat.security.alias.ClearExpiredAliasTokens
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.alias;

import org.hibernate.SessionFactory;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.services.AliasTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.inject.Inject;

public class ClearExpiredAliasTokens implements Runnable {
    public ClearExpiredAliasTokens(final AliasTokenService aliasTokenService, final SiteConfigPreferences preferences, final JdbcTemplate template) {
        if (_log.isDebugEnabled()) {
            _log.debug("Initializing the alias token sweeper job");
        }
        _service = aliasTokenService;
        _preferences=preferences;
        _template = template;
    }

    /**
     */
    @Override
    public void run() {
        if (_log.isDebugEnabled()) {
            _log.debug("Executing alias token sweep function");
        }
        _service.invalidateExpiredTokens(_preferences.getAliasTokenTimeout());
    }
    private final SiteConfigPreferences   _preferences;

    @Inject
    private SessionFactory _sessionFactory;

    private final AliasTokenService         _service;

    private static final Logger       _log                            = LoggerFactory.getLogger(ClearExpiredAliasTokens.class);

    private final JdbcTemplate _template;
}
