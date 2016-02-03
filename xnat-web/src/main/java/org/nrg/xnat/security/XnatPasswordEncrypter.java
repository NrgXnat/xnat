/*
 * org.nrg.xnat.security.XnatPasswordEncrypter
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 12/11/13 3:33 PM
 */
package org.nrg.xnat.security;/*
 * org.nrg.xnat.helpers.prearchive.PrearcDatabase
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Created 12/4/13 3:12 PM
 */

import org.nrg.xft.db.PoolDBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class XnatPasswordEncrypter {
    private static Logger logger = LoggerFactory.getLogger(XnatPasswordEncrypter.class);

    public static void execute() {
        try {
            ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);

            Map<Integer, String> userPasswords = new HashMap<Integer, String>();
            ResultSet rs= new PoolDBUtils().executeQuery(null, "SELECT xdat_user_id, primary_password FROM xdat_user WHERE primary_password IS NOT NULL AND length(primary_password) != 64", null);
            while(rs.next()){
                int id = rs.getInt(1);
                String password = rs.getString(2);
                String encodedPassword = encoder.encodePassword(password, null);
                userPasswords.put(id, encodedPassword);
            }

            Map<Integer, String> historyPasswords = new HashMap<Integer, String>();
            ResultSet rs2 = new PoolDBUtils().executeQuery(null, "SELECT history_id, primary_password FROM xdat_user_history WHERE primary_password IS NOT NULL AND length(primary_password) != 64", null);
            while(rs2.next()){
                int id = rs2.getInt(1);
                String password = rs2.getString(2);
                String encodedPassword = encoder.encodePassword(password, null);
                historyPasswords.put(id, encodedPassword);
            }

            for (int userId : userPasswords.keySet()) {
                new PoolDBUtils().executeNonSelectQuery("UPDATE xdat_user SET primary_password = '" + userPasswords.get(userId) + "' WHERE xdat_user_id = " + userId, null, null);
            }

            for (int historyId : historyPasswords.keySet()) {
                new PoolDBUtils().executeNonSelectQuery("UPDATE xdat_user_history SET primary_password = '" + historyPasswords.get(historyId) + "' WHERE history_id = " + historyId, null, null);
            }

            if (!userPasswords.isEmpty() || !historyPasswords.isEmpty()) {
                new PoolDBUtils().executeNonSelectQuery("DELETE FROM xs_item_cache", null, null);
            }
        } catch (Exception e) {
            logger.error("",e);
        }
    }
}
