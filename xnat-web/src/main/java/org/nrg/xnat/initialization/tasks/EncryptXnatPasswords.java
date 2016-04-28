/*
 * org.nrg.xnat.initialization.tasks.XnatPasswordEncrypter
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xnat.initialization.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class EncryptXnatPasswords extends AbstractInitializingTask {
    @Override
    public String getTaskName() {
        return "Encrypt XNAT passwords";
    }

    @Override
    public void run() {
        try {
            final PasswordResultSetExtractor extractor = new PasswordResultSetExtractor();
            final Map<Integer, String> userPasswords = _template.query("SELECT xdat_user_id, primary_password FROM xdat_user WHERE primary_password IS NOT NULL AND length(primary_password) != 64", extractor);
            final Map<Integer, String> historyPasswords = _template.query("SELECT history_id, primary_password FROM xdat_user_history WHERE primary_password IS NOT NULL AND length(primary_password) != 64", extractor);

            for (final int userId : userPasswords.keySet()) {
                _template.update("UPDATE xdat_user SET primary_password = ? WHERE xdat_user_id = ?", userPasswords.get(userId), userId);
            }

            for (int historyId : historyPasswords.keySet()) {
                _template.update("UPDATE xdat_user_history SET primary_password = ? WHERE history_id = ?", userPasswords.get(historyId), historyId);
            }

            if ((!userPasswords.isEmpty() || !historyPasswords.isEmpty()) && tableExists("xs_item_cache")) {
                _template.update("DELETE FROM xs_item_cache");
            }

            complete();
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    private boolean tableExists(final String name) throws SQLException {
        try (final Connection connection = _template.getDataSource().getConnection();
             final ResultSet results = connection.getMetaData().getTables("catalog", null, name, new String[]{"table"})) {
            if (results.next()) {
                return true;
            }
        }
        return false;
    }

    private static class PasswordResultSetExtractor implements ResultSetExtractor<Map<Integer, String>> {
        @Override
        public Map<Integer, String> extractData(final ResultSet results) throws SQLException, DataAccessException {
            final Map<Integer, String> passwords = new HashMap<>();
            while (results.next()) {
                passwords.put(results.getInt(1), encoder.encodePassword(results.getString(2), null));
            }
            return passwords;
        }
        final ShaPasswordEncoder encoder = new ShaPasswordEncoder(256);
    }

    private static Logger logger = LoggerFactory.getLogger(EncryptXnatPasswords.class);

    @Autowired
    @Lazy
    private JdbcTemplate _template;
}
