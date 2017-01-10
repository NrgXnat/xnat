/*
 * web: org.nrg.xnat.initialization.tasks.EncryptXnatPasswords
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import com.google.common.collect.Maps;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.security.UserAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Component
public class EncryptXnatPasswords extends AbstractInitializingTask {
    @Autowired
    public EncryptXnatPasswords(final JdbcTemplate template) {
        super();
        _template = template;
        _helper = new DatabaseHelper(template);
    }

    @Override
    public String getTaskName() {
        return "Encrypt XNAT passwords";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        try {
            if (_helper.tableExists("xdat_user")) {
                try {
                    final PasswordResultSetExtractor extractor = new PasswordResultSetExtractor();

                    final Map<Integer, Map<UserAttributes, String>> userPasswords    = _template.query("SELECT xdat_user_id, primary_password FROM xdat_user WHERE primary_password IS NOT NULL AND length(primary_password) != 64", extractor);
                    final Map<Integer, Map<UserAttributes, String>> historyPasswords = _template.query("SELECT history_id, primary_password FROM xdat_user_history WHERE primary_password IS NOT NULL AND length(primary_password) != 64", extractor);

                    for (final int userId : userPasswords.keySet()) {
                        final Map<UserAttributes, String> attributes = userPasswords.get(userId);
                        _template.update("UPDATE xdat_user SET primary_password = ?, salt = ?, primary_password_encrypt = 1 WHERE xdat_user_id = ?", attributes.get(UserAttributes.password), attributes.get(UserAttributes.salt), userId);
                    }

                    for (int historyId : historyPasswords.keySet()) {
                        final Map<UserAttributes, String> attributes = historyPasswords.get(historyId);
                        _template.update("UPDATE xdat_user_history SET primary_password = ?, salt = ?, primary_password_encrypt = 1 WHERE history_id = ?", attributes.get(UserAttributes.password), attributes.get(UserAttributes.salt), historyId);
                    }

                    if ((!userPasswords.isEmpty() || !historyPasswords.isEmpty()) && _helper.tableExists("xs_item_cache")) {
                        _template.update("DELETE FROM xs_item_cache");
                    }
                } catch (BadSqlGrammarException e) {
                    throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
                } catch (Exception e) {
                    throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to access the database and update the user table password encryption.", e);
                }
            } else {
                throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
            }
        } catch (SQLException e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Error, "An SQL error occurred trying to test for the existence of the xdat_user table.", e);
        }
    }

    private static class PasswordResultSetExtractor implements ResultSetExtractor<Map<Integer, Map<UserAttributes, String>>> {
        @Override
        public Map<Integer, Map<UserAttributes, String>> extractData(final ResultSet results) throws SQLException, DataAccessException {
            final Map<Integer, Map<UserAttributes, String>> passwords = Maps.newHashMap();
            while (results.next()) {
                final String                      salt       = Users.createNewSalt();
                final Map<UserAttributes, String> attributes = Maps.newHashMap();
                attributes.put(UserAttributes.salt, salt);
                attributes.put(UserAttributes.password, Users.encode(results.getString(2), salt));
                passwords.put(results.getInt(1), attributes);
            }
            return passwords;
        }
    }

    private final DatabaseHelper _helper;
    private final JdbcTemplate   _template;
}
