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

import java.sql.Timestamp;
import java.util.Date;
import java.util.Hashtable;

import org.nrg.xft.XFTTable;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.UserI;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

public class HistoricPasswordValidator implements PasswordValidator {

	private String message="Password has been used previously.";
	private int durationInDays = 365;
	
	@Override
	public boolean isValid(String password, UserI user) {
		//if there's no user, they're probably new so there's nothing to do here.
		if(user != null){
            try {
                String userId = user.getUsername();
                String dbName = user.getDBName();
                Date today = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).getTime();
                Timestamp lastYearsTimestamp = new java.sql.Timestamp(today.getTime() -  durationInDays * 86400000L);// 24 * 60 * 60 * 1000);    //31557600000L);
                String query = "SELECT primary_password AS hashed_password, salt AS salt FROM xdat_user_history WHERE login='" + userId + "' AND change_date > '" + lastYearsTimestamp + "' UNION SELECT primary_password AS password, salt AS salt FROM xdat_user WHERE login='" + userId + "';";
                XFTTable table = TableSearch.Execute(query, dbName, userId);
                table.resetRowCursor();
                while (table.hasMoreRows()) {
                    Hashtable row = table.nextRowHash();
                    String hashedPassword = (String) row.get("hashed_password");
                    String salt = (String) row.get("salt");
                    String encrypted = new ShaPasswordEncoder(256).encodePassword(password, salt);
                    if (encrypted.equals(hashedPassword)) {
                        message = "Password has been used in the previous " + durationInDays + " days.";
                        return false;
                    }
                }
            } catch (Exception e) {
                message = e.getMessage();
                return false;
            }
        }
        return true;
	}
	public String getMessage() {
		return message;
	}
	public int getDurationInDays() {
		return durationInDays;
	}
	public void setDurationInDays(int durationInDays) {
		this.durationInDays = durationInDays;
	}

}
