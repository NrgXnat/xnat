package org.nrg.xdat.security;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.turbine.util.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xft.db.PoolDBUtils;
import org.springframework.jdbc.core.JdbcTemplate;

public class HistoricPasswordValidator implements PasswordValidator {

	private String message="Password has been used previously.";
	private int durationInDays = 365;
	
	@Override
	public boolean isValid(String password, XDATUser user) {
		//if there's no user, they're probably new so there's nothing to do here.
		if(user == null){
			return true;
		}
		String encrypted = XDATUser.EncryptString(password,"SHA-256");
		Date today = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).getTime();
		Timestamp lastYearsTimestamp = new java.sql.Timestamp(today.getTime() -  durationInDays * 86400000L);// 24 * 60 * 60 * 1000);    //31557600000L);
		
		String userId = user.getUsername();
		String dbName = user.getDBName();
		String query = "SELECT count(login) as prevPasswords from (SELECT login FROM xdat_user_history WHERE change_date > '" + lastYearsTimestamp + "' and (primary_password='" + encrypted + "' OR primary_password='" + password + "') and login='" + userId + "' UNION SELECT login FROM xdat_user WHERE (primary_password='" + encrypted + "' OR primary_password='" + password + "') and login='" + userId + "') LOGINS;";
		try{
			//moved current password comparison into SQL statement (sometimes the user object is passed in with the already modified password).  It's whats in the db that matters.
			
			Long count = (Long)PoolDBUtils.ReturnStatisticQuery(query, "prevPasswords", dbName,null);

			if(count == 0){
				return true;
			} else {
				message = "Password has been used in the previous " + durationInDays + " days.";
				return false;
			}
		} catch (SQLException e) {
            //logger.error("",e);
			message = e.getMessage();
            return false;
        } catch (Exception e) {
            //logger.error("",e);
        	message = e.getMessage();
            return false;
        }
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
