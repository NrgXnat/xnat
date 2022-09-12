/*
 * core: org.nrg.xdat.security.validators.HistoricPasswordValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.validators;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

import static org.nrg.framework.orm.DatabaseHelper.convertPGIntervalToSeconds;

@Component
@Slf4j
public class HistoricPasswordValidator implements PasswordValidator {

    @Autowired
    public HistoricPasswordValidator(final PasswordEncoder passwordEncoder, final SiteConfigPreferences preferences, final NamedParameterJdbcTemplate template) {
        _passwordEncoder = passwordEncoder;
        _preferences = preferences;
        _template = template;
    }

    /**
     * Package-protected access level constructor is provided for "panic mode" instantiation when context can't be
     * found. Default values are then used for all preference settings.
     */
    HistoricPasswordValidator() {
        _passwordEncoder = null;
        _preferences = null;
        _template = null;
    }

    @Override
    public String isValid(final String password, final UserI user) {
        // If there's no user, they're probably new so there's nothing to do here. If there's none of the other stuff,
        // we can't check anyway.
        if (user == null || _passwordEncoder == null || _preferences == null || _template == null) {
            return "";
        }

        final String restriction = getPasswordReuseRestriction();
        if (!StringUtils.equals(restriction, "Historical")) {
            return "";
        }
        try {
            final MapSqlParameterSource parameters = new MapSqlParameterSource("userId", user.getUsername());
            parameters.addValue("startOfDurationTimestamp", getDurationTimestamp());
            return _template.queryForList(QUERY, parameters, String.class).stream().anyMatch(hash -> _passwordEncoder.matches(password, hash))
                   ? "Password has been used in the previous " + getPasswordHistoryDuration() + "."
                   : "";
        } catch (Exception e) {
            log.error("An error occurred while trying to validate a password update for user " + user.getLogin(), e);
            return "An error occurred trying to update your password. Please contact your system administrator.";
        }
    }

    private String getPasswordReuseRestriction() {
        return _preferences != null ? _preferences.getPasswordReuseRestriction() : "None";
    }

    private String getPasswordHistoryDuration() {
        return _preferences != null ? _preferences.getPasswordHistoryDuration() : "1 year";
    }

    private Timestamp getDurationTimestamp() {
        final String duration          = getPasswordHistoryDuration();
        final long   durationInSeconds = convertPGIntervalToSeconds(duration);
        log.debug("Found duration \"{}\", which is {} seconds", duration, durationInSeconds);
        return new Timestamp(Calendar.getInstance(TimeZone.getDefault()).getTime().getTime() - (durationInSeconds * 1000L));
    }

    private static final String QUERY = "SELECT primary_password AS hashed_password FROM xdat_user_history WHERE login = :userId  AND change_date > :startOfDurationTimestamp UNION SELECT primary_password AS password FROM xdat_user WHERE login = :userId";

    private final PasswordEncoder            _passwordEncoder;
    private final SiteConfigPreferences      _preferences;
    private final NamedParameterJdbcTemplate _template;
}
