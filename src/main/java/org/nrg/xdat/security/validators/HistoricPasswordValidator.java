/*
 * core: org.nrg.xdat.security.HistoricPasswordValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.validators;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.XFTTable;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;

@Component
public class HistoricPasswordValidator implements PasswordValidator {
    @Autowired
    public HistoricPasswordValidator(final SiteConfigPreferences preferences) {
        _preferences = preferences;
    }

    /**
     * Package-protected access level constructor is provided for "panic mode" instantiation when context can't be
     * found. Default values are then used for all preference settings.
     */
    HistoricPasswordValidator() {
        _preferences = null;
    }

    @Override
    public boolean isValid(String password, UserI user) {
        //if there's no user, they're probably new so there's nothing to do here.
        if (user != null) {
            final String passwordReuseRestriction = getPasswordReuseRestriction();
            if (StringUtils.equals(passwordReuseRestriction, "Historical")) {
                try {
                    final long      durationInSeconds        = SiteConfigPreferences.convertPGIntervalToSeconds(getPasswordHistoryDuration());
                    final String    userId                   = user.getUsername();
                    final String    dbName                   = user.getDBName();
                    final Date      today                    = Calendar.getInstance(TimeZone.getDefault()).getTime();
                    final Timestamp startOfDurationTimestamp = new Timestamp(today.getTime() - (durationInSeconds * 1000L));// Multiplying by 1000 to convert to milliseconds;
                    final String    query                    = "SELECT primary_password AS hashed_password, salt AS salt FROM xdat_user_history WHERE login='" + userId + "' AND change_date > '" + startOfDurationTimestamp + "' UNION SELECT primary_password AS password, salt AS salt FROM xdat_user WHERE login='" + userId + "';";
                    final XFTTable  table                    = TableSearch.Execute(query, dbName, userId);

                    table.resetRowCursor();
                    while (table.hasMoreRows()) {
                        final Hashtable row            = table.nextRowHash();
                        final String    hashedPassword = (String) row.get("hashed_password");
                        final String    salt           = (String) row.get("salt");
                        final String    encrypted      = Users.encode(password, salt);
                        if (encrypted.equals(hashedPassword)) {
                            _message = "Password has been used in the previous " + getPasswordHistoryDuration() + ".";
                            break;
                        }
                    }
                } catch (Exception e) {
                    _message = e.getMessage();
                }
            }
        }

        return StringUtils.isBlank(_message);
    }

    private String getPasswordReuseRestriction() {
        return _preferences != null ? _preferences.getPasswordReuseRestriction() : "None";
    }

    private String getPasswordHistoryDuration() throws SQLException {
        return _preferences != null ? _preferences.getPasswordHistoryDuration() : "1 year";
    }

    public String getMessage() {
        return _message;
    }

    private final SiteConfigPreferences _preferences;

    private String _message;
}
