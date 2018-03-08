/*
 * web: org.nrg.xnat.security.ResetFailedLogins
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xnat.task.AbstractXnatRunnable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.SQLException;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
@Slf4j
@Getter(AccessLevel.PROTECTED)
@Accessors(prefix = "_")
public class ResetFailedLogins extends AbstractXnatRunnable {
    public ResetFailedLogins(final NamedParameterJdbcTemplate template, final int maxFailedLogins, final String maxFailedLoginsLockoutDuration) {
        _template = template;
        _helper = new DatabaseHelper((JdbcTemplate) _template.getJdbcOperations());
        _sqlParameterSource = new MapSqlParameterSource("maxFailedLogins", maxFailedLogins).addValue("duration", maxFailedLoginsLockoutDuration);
    }

    @Override
    protected void runTask() {
        // If we don't have the table marked as existing...
        if (!_userAuthTableExists) {
            try {
                // Then let's check again. Maybe it exists now!
                _userAuthTableExists = _helper.tableExists("xhbm_xdat_user_auth");
            } catch (SQLException e) {
                log.warn("An error occurred trying to check whether the xhbm_xdat_user_auth table exists", e);
            }
        }

        // OK, if it exists now...
        if (_userAuthTableExists) {
            // Update any rows where their failed logins exceeds the configured max
            // but the last failure was longer ago than the max lockout time.
            final int updated = _template.update(QUERY, getSqlParameterSource());
            log.info("Reset {} failed login attempts.", updated);
        } else {
            log.info("Didn't reset any failed login attempts, there's no data in the relevant table.");
        }
    }

    private static final String QUERY = "UPDATE xhbm_xdat_user_auth SET failed_login_attempts = 0, lockout_time = NULL WHERE failed_login_attempts >= :maxFailedLogins AND lockout_time < NOW() - :duration::INTERVAL";

    private final NamedParameterJdbcTemplate _template;
    private final DatabaseHelper             _helper;
    private final SqlParameterSource         _sqlParameterSource;

    private boolean _userAuthTableExists;
}
