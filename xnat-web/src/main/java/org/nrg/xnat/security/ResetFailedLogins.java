/*
 * web: org.nrg.xnat.security.ResetFailedLogins
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class ResetFailedLogins implements Runnable {

    public ResetFailedLogins(final JdbcTemplate template, final SiteConfigPreferences preferences) {
        _template = template;
        _preferences = preferences;
    }

    @Override
    public void run() {
        if (_template.queryForObject("SELECT count(*) from xhbm_xdat_user_auth", Integer.TYPE) > 0) {
            final int updated = _template.update("UPDATE xhbm_xdat_user_auth SET failed_login_attempts = 0, lockout_time = NULL WHERE failed_login_attempts >= "+_preferences.getMaxFailedLogins()+" AND lockout_time < NOW() - INTERVAL '" + _preferences.getMaxFailedLoginsLockoutDuration() + "'");
            if (_log.isInfoEnabled()) {
                _log.info("Reset {} failed login attempts.", updated);
            }
        } else {
            _log.info("Didn't reset any failed login attempts, there's no data in the relevant table.");
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(ResetFailedLogins.class);

    private final JdbcTemplate _template;
    private final SiteConfigPreferences _preferences;
}
