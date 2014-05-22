package org.nrg.xdat.security;

import org.nrg.xft.XFTTable;
import org.nrg.xft.search.TableSearch;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Hashtable;

public class HistoricPasswordValidator implements PasswordValidator {

	private String _message="Password has been used previously.";
	private int _durationInDays = 365;

    @SuppressWarnings("unused")
    public HistoricPasswordValidator() {
        //
    }

    public HistoricPasswordValidator(final int durationInDays) {
        setDurationInDays(durationInDays);
    }

    @Override
    public boolean isValid(String password, XDATUser user) {
        //if there's no user, they're probably new so there's nothing to do here.
        if (user != null) {
            try {
                String userId = user.getUsername();
                String dbName = user.getDBName();
                Date today = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).getTime();
                Timestamp lastYearsTimestamp = new java.sql.Timestamp(today.getTime() - getDurationInDays() * 86400000L);// 24 * 60 * 60 * 1000);    //31557600000L);
                String query = "SELECT primary_password AS hashed_password, salt AS salt FROM xdat_user_history WHERE login='" + userId + "' AND change_date > '" + lastYearsTimestamp + "' UNION SELECT primary_password AS password, salt AS salt FROM xdat_user WHERE login='" + userId + "';";
                XFTTable table = TableSearch.Execute(query, dbName, userId);
                table.resetRowCursor();
                while (table.hasMoreRows()) {
                    Hashtable row = table.nextRowHash();
                    String hashedPassword = (String) row.get("hashed_password");
                    String salt = (String) row.get("salt");
                    String encrypted = new ShaPasswordEncoder(256).encodePassword(password, salt);
                    if (encrypted.equals(hashedPassword)) {
                        setMessage("Password has been used in the previous " + _durationInDays + " days.");
                        return false;
                    }
                }
            } catch (Exception e) {
                setMessage(e.getMessage());
                return false;
            }
        }
        return true;
    }
	public String getMessage() {
		return _message;
	}
    public void setMessage(final String message) {
        _message = message;
    }
	public int getDurationInDays() {
		return _durationInDays;
	}
	public void setDurationInDays(final int durationInDays) {
		_durationInDays = durationInDays;
	}
}
