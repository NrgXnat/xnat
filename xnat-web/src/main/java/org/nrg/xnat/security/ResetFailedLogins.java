/*
 * org.nrg.xnat.security.ResetFailedLoginsJob
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.concurrent.Callable;

public class ResetFailedLogins implements Callable<Void> {

    public ResetFailedLogins(final DataSource dataSource) {
        _dataSource = dataSource;
    }

    @Override
    public Void call() {
        JdbcTemplate template = new JdbcTemplate(_dataSource);
        template.execute("UPDATE xhbm_xdat_user_auth SET failed_login_attempts=0");
        _log.info("Reset all failed login attempts.");
        return null;
    }

    private static final Logger _log = LoggerFactory.getLogger(ResetFailedLogins.class);

    private final DataSource _dataSource;
}

