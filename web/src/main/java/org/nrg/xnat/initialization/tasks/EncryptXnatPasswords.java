/*
 * web: org.nrg.xnat.initialization.tasks.EncryptXnatPasswords
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import com.google.common.collect.ImmutableMap;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xdat.security.helpers.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class EncryptXnatPasswords extends AbstractInitializingTask {
    private static final String              ITEM_ID                                = "itemId";
    private static final String              PASSWORD                               = "password";
    private static final String              QUERY_TEMPLATE_GET_OLD_PASSWORDS       = "SELECT ${ID}, primary_password "
                                                                                      + "FROM ${TABLE} "
                                                                                      + "WHERE primary_password IS NOT NULL "
                                                                                      + "  AND salt IS NULL "
                                                                                      + "  AND primary_password !~ '^([0-9A-Fa-f]{64}(\\{[A-Za-z0-9]{64}\\})?|\\{[A-z0-9-]{3,7}}.*)$'";
    private static final String              QUERY_TEMPLATE_UPDATE_OLD_PASSWORDS    = "UPDATE ${TABLE} "
                                                                                      + "SET primary_password         = :" + PASSWORD + ", "
                                                                                      + "    salt                     = NULL, "
                                                                                      + "    primary_password_encrypt = 1 "
                                                                                      + "WHERE ${ID} = :" + ITEM_ID;
    private static final String              QUERY_TEMPLATE_ENCODE_SALTED_PASSWORDS = "UPDATE ${TABLE} "
                                                                                      + "SET primary_password         = primary_password || '{' || salt || '}', "
                                                                                      + "    salt                     = null, "
                                                                                      + "    primary_password_encrypt = 1 "
                                                                                      + "WHERE primary_password ~ '^[A-Fa-f0-9]{64}$' "
                                                                                      + "  AND salt ~ '^[A-Za-z0-9]{64}$'";
    private static final Map<String, String> USER_VARIABLES                         = ImmutableMap.of("ID", "xdat_user_id", "TABLE", "xdat_user");
    private static final Map<String, String> HISTORY_VARIABLES                      = ImmutableMap.of("ID", "history_id", "TABLE", "xdat_user_history");
    private static final String              QUERY_GET_OLD_PASSWORDS                = StringSubstitutor.replace(QUERY_TEMPLATE_GET_OLD_PASSWORDS, USER_VARIABLES);
    private static final String              QUERY_UPDATE_OLD_PASSWORDS             = StringSubstitutor.replace(QUERY_TEMPLATE_UPDATE_OLD_PASSWORDS, USER_VARIABLES);
    private static final String              QUERY_GET_OLD_PASSWORD_HISTORY         = StringSubstitutor.replace(QUERY_TEMPLATE_GET_OLD_PASSWORDS, HISTORY_VARIABLES);
    private static final String              QUERY_UPDATE_OLD_PASSWORD_HISTORY      = StringSubstitutor.replace(QUERY_TEMPLATE_UPDATE_OLD_PASSWORDS, HISTORY_VARIABLES);
    private static final String              QUERY_ENCODE_SALTED_PASSWORDS          = StringSubstitutor.replace(QUERY_TEMPLATE_ENCODE_SALTED_PASSWORDS, USER_VARIABLES);
    private static final String              QUERY_ENCODE_SALTED_PASSWORD_HISTORY   = StringSubstitutor.replace(QUERY_TEMPLATE_ENCODE_SALTED_PASSWORDS, HISTORY_VARIABLES);
    private static final String              QUERY_GUEST_PASSWORD                   = "UPDATE xdat_user "
                                                                                      + "SET primary_password         = '{bcrypt}$2a$10$RlNYXWR/JwKLLjgeIny6QuoRvYRp16OnjiQOY6n8gEUTXOvS6OF4a', "
                                                                                      + "    salt                     = null, "
                                                                                      + "    primary_password_encrypt = 1 "
                                                                                      + "WHERE login = 'guest'";
    private static final String              QUERY_CLEAR_XS_ITEM_CACHE              = "DELETE FROM xs_item_cache";

    private final DatabaseHelper             _helper;
    private final NamedParameterJdbcTemplate _template;

    @Autowired
    public EncryptXnatPasswords(final NamedParameterJdbcTemplate template) {
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

                    final Map<String, ?>[] oldUserPasswords    = _template.query(QUERY_GET_OLD_PASSWORDS, extractor);
                    final boolean          hasOldUserPasswords = oldUserPasswords.length > 0;
                    if (hasOldUserPasswords) {
                        log.info("Found {} password entries that needed to be updated", oldUserPasswords.length);
                        _template.batchUpdate(QUERY_UPDATE_OLD_PASSWORDS, oldUserPasswords);
                    }

                    final Map<String, ?>[] oldHistoryPasswords    = _template.query(QUERY_GET_OLD_PASSWORD_HISTORY, extractor);
                    final boolean          hasOldHistoryPasswords = oldHistoryPasswords.length > 0;
                    if (hasOldHistoryPasswords) {
                        log.info("Found {} password history entries that needed to be updated", oldHistoryPasswords.length);
                        _template.batchUpdate(QUERY_UPDATE_OLD_PASSWORD_HISTORY, oldHistoryPasswords);
                    }

                    final boolean updatedSaltedPasswords       = _template.update(QUERY_ENCODE_SALTED_PASSWORDS, EmptySqlParameterSource.INSTANCE) > 0;
                    final boolean updatedSaltedPasswordHistory = _template.update(QUERY_ENCODE_SALTED_PASSWORD_HISTORY, EmptySqlParameterSource.INSTANCE) > 0;
                    if ((hasOldUserPasswords || hasOldHistoryPasswords || updatedSaltedPasswords || updatedSaltedPasswordHistory) && _helper.tableExists("xs_item_cache")) {
                        // Only update the guest if other passwords were updated.
                        _template.update(QUERY_GUEST_PASSWORD, EmptySqlParameterSource.INSTANCE);

                        // Clear the cache, which contains old versions of encrypted/salted passwords.
                        final int updated = _template.update(QUERY_CLEAR_XS_ITEM_CACHE, EmptySqlParameterSource.INSTANCE);
                        log.info("Found passwords and/or password histories that needed to be updated, so updated guest user and cleared {} rows from xs_item_cache", updated);
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

    @Setter
    private static class PasswordResultSetExtractor implements ResultSetExtractor<Map<String, ?>[]> {
        @Override
        public Map<String, ?>[] extractData(final ResultSet results) throws SQLException, DataAccessException {
            final List<Map<String, Object>> passwords = new ArrayList<>();
            while (results.next()) {
                final Map<String, Object> map = new HashMap<>();
                map.put(ITEM_ID, results.getInt(1));
                map.put(PASSWORD, Users.encode(results.getString(2)));
                passwords.add(map);
            }
            //noinspection unchecked
            return passwords.toArray(new Map[0]);
        }
    }
}
