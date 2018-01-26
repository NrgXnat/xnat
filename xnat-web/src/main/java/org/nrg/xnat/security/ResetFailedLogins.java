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
import org.nrg.xnat.task.AbstractXnatRunnable;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
@Slf4j
@Getter(AccessLevel.PROTECTED)
@Accessors(prefix = "_")
public class ResetFailedLogins extends AbstractXnatRunnable {
    public ResetFailedLogins(final NamedParameterJdbcTemplate template, final int maxFailedLogins, final String maxFailedLoginsLockoutDuration) {
        _template = template;
        _sqlParameterSource = new MapSqlParameterSource("maxFailedLogins", maxFailedLogins).addValue("duration", maxFailedLoginsLockoutDuration);
    }

    @Override
    protected void runTask() {
        if (_template.queryForObject("SELECT count(*) FROM xhbm_xdat_user_auth", NO_PARAMS, Integer.TYPE) > 0) {
            final int updated = _template.update(QUERY, getSqlParameterSource());
            log.info("Reset {} failed login attempts.", updated);
        } else {
            log.info("Didn't reset any failed login attempts, there's no data in the relevant table.");
        }
    }

    private static final SqlParameterSource NO_PARAMS = new EmptySqlParameterSource();
    private static final String             QUERY     = "UPDATE xhbm_xdat_user_auth SET failed_login_attempts = 0, lockout_time = NULL WHERE failed_login_attempts >= :maxFailedLogins AND lockout_time < NOW() - :duration::INTERVAL";

    private final NamedParameterJdbcTemplate _template;
    private final SqlParameterSource         _sqlParameterSource;
}
