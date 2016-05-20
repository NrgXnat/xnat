/*
 * org.nrg.xdat.security.HistoricPasswordValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 12/11/13 3:34 PM
 */
package org.nrg.xdat.security;

import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.XFTTable;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Hashtable;

@Component
public class HistoricPasswordValidator implements PasswordValidator {
    @Override
    public boolean isValid(String password, UserI user) {
        //if there's no user, they're probably new so there's nothing to do here.
        if (user != null) {
            try {
                int durationInDays = _preferences.getPasswordHistoryDuration();
                String userId = user.getUsername();
                String dbName = user.getDBName();
                Date today = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).getTime();
                Timestamp lastYearsTimestamp = new java.sql.Timestamp(today.getTime() - durationInDays * 86400000L);// 24 * 60 * 60 * 1000);    //31557600000L);
                String query = "SELECT primary_password AS hashed_password, salt AS salt FROM xdat_user_history WHERE login='" + userId + "' AND change_date > '" + lastYearsTimestamp + "' UNION SELECT primary_password AS password, salt AS salt FROM xdat_user WHERE login='" + userId + "';";
                XFTTable table = TableSearch.Execute(query, dbName, userId);
                table.resetRowCursor();
                while (table.hasMoreRows()) {
                    Hashtable row = table.nextRowHash();
                    String hashedPassword = (String) row.get("hashed_password");
                    String salt = (String) row.get("salt");
                    String encrypted = new ShaPasswordEncoder(256).encodePassword(password, salt);
                    if (encrypted.equals(hashedPassword)) {
                        _message = getDefaultMessage();
                        return false;
                    }
                }
            } catch (Exception e) {
                _message = e.getMessage();
                return false;
            }
        }
        return true;
    }

    public String getMessage() {
        return _message;
    }

    private String getDefaultMessage() {
        return String.format("Password has been used in the previous %d days.", _preferences.getPasswordHistoryDuration());
    }

    @Autowired
    @Lazy
    private SiteConfigPreferences _preferences;

    private String _message;
}
