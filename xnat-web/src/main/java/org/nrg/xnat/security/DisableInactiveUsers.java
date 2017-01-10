/*
 * web: org.nrg.xnat.security.DisableInactiveUsers
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import org.apache.commons.lang3.time.DateUtils;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.utils.XnatUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DisableInactiveUsers implements Runnable {

    public DisableInactiveUsers(final XnatUserProvider userProvider, final int inactivityBeforeLockout, final int lockoutDuration) {
        _userProvider = userProvider;
        _inactivityBeforeLockout = inactivityBeforeLockout;
        _lockoutDuration = lockoutDuration;
    }

    /**
     * Finds user accounts that have not been validated or authenticated within the indicated time frame
     * and disables them.
     */
    @Override
    public void run() {
        try {
            final UserI adminUser = _userProvider.get();

            final XFTTable table = XFTTable.Execute("SELECT xdat_user.login FROM xdat_user INNER JOIN " +
                                                    "(" +
                                                    "SELECT y.login, last_login, activation_date FROM xdat_user_meta_data INNER JOIN " +
                                                    "(" +
                                                    "SELECT xdat_user.login, xdat_user.xdat_user_id, MAX(xdat_user_login.login_date) AS last_login FROM xdat_user_login RIGHT JOIN xdat_user ON xdat_user_login.user_xdat_user_id=xdat_user.xdat_user_id GROUP BY xdat_user.login,xdat_user.xdat_user_id" +
                                                    ") y " + //get last login times for each user
                                                    "ON y.xdat_user_id=xdat_user_meta_data.meta_data_id AND y.login NOT IN (SELECT username FROM xhbm_user_role WHERE role='Administrator') AND y.xdat_user_id NOT IN (SELECT xdat_user_xdat_user_id FROM xdat_r_xdat_role_type_assign_xdat_user WHERE xdat_r_xdat_role_type_assign_xdat_user.xdat_role_type_role_name = 'Administrator')" +
                                                    ") x " + //get dates that each non-admin user was created
                                                    "ON x.login=xdat_user.login AND ((x.activation_date<(now()- INTERVAL '" + _inactivityBeforeLockout + " seconds')) AND ((x.last_login IS NULL) OR x.last_login<(now()- INTERVAL '" + _inactivityBeforeLockout + " seconds'))) AND xdat_user.enabled=1", null, null);

            table.resetRowCursor();
            while (table.hasMoreRows()) {
                final Object[] row = table.nextRow();

                final String username = (String) row[0];
                try {
                    final UserI u = Users.getUser(username);

                    // Fixes XNAT-2407. Only disable user if they have not been recently modified (enabled).
                    // Also do not disable the guest user.
                    if (!hasUserBeenModified(u, _inactivityBeforeLockout) && !username.equals("guest")) {
                        u.setEnabled("0");
                        u.setVerified("0");
                        Users.save(u, u, false, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.PROCESS, "Disabled due to inactivity"));

                        String expiration = TurbineUtils.getDateTimeFormatter().format(DateUtils.addMilliseconds(GregorianCalendar.getInstance().getTime(), _lockoutDuration));
                        System.out.println("Locked out " + u.getLogin() + " user account until " + expiration);
                        AdminUtils.sendAdminEmail(u.getLogin() + " account disabled due to inactivity.", "User " + u.getLogin() + " has been automatically disabled due to inactivity.");
                    }
                } catch (InvalidPermissionException e) {
                    if (e.getMessage().contains("wrk:workflowData")) {
                        logger.warn("An invalid permission exception was encountered while attempting to disable the user {} with provided user {}. This probably indicates that the system is still initializing. If it occurs frequently after the system has started, there may be an issue with your database schema or data.");
                    } else {
                        logger.error("An unexpected invalid permission exception occurred", e);
                    }
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        } catch (SQLException e) {
            logger.error("An error occurred querying the database: [" + e.getErrorCode() + "] " + e.getSQLState(), e);
        } catch (DBPoolException e) {
            logger.error("A database connection or pooling error occurred.", e);
        }
    }

    /**
     * Function determines if the user has been modified in the past amount of seconds.
     * Fixes XNAT-2407. This keeps the job from disabling a user if the admin has just enabled (modified) them.
     *
     * @param u       - the user we are interested in.
     * @param seconds - Has the user been modified in the past amount of seconds.
     *
     * @return true if the user has been modified / otherwise false.
     */
    private boolean hasUserBeenModified(final UserI u, final int seconds) {

        // Subtract seconds from today's date.
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND, -seconds);

        // If the time is before the last modified date, the user has been modified.
        final Date lastModified = u.getLastModified();
        return lastModified != null && (c.getTime().before(lastModified));
    }

    private static final Logger logger = LoggerFactory.getLogger(DisableInactiveUsers.class);

    private final XnatUserProvider _userProvider;
    private final int              _inactivityBeforeLockout;
    private final int              _lockoutDuration;
}

