/*
 * web: org.nrg.xnat.initialization.tasks.UpdateUserAuthTable
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.services.XdatUserAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Adds users from /old xdat_user table to new user authentication table if they are not already there. New local
 * database users now get added to both automatically, but this is necessary so that those who upgrade from an earlier
 * version still have their users be able to log in. Password expiry times are also added so that pre-existing users
 * still have their passwords expire.
 */
@SuppressWarnings("SqlDialectInspection")
@Component
public class UpdateUserAuthTable extends AbstractInitializingTask {
    @Autowired
    public UpdateUserAuthTable(final JdbcTemplate template, final XdatUserAuthService xdatUserAuthService) {
        super();
        _template = template;
        _xdatUserAuthService = xdatUserAuthService;
    }

    @Override
    public String getTaskName() {
        return "Update the user authentication table";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        try {
            final List<XdatUserAuth> unmapped = _template.query("SELECT login, enabled FROM xdat_user WHERE login NOT IN (SELECT xdat_username FROM xhbm_xdat_user_auth)", new RowMapper<XdatUserAuth>() {
                @Override
                public XdatUserAuth mapRow(final ResultSet resultSet, final int i) throws SQLException {
                    final String login = resultSet.getString("login");
                    final boolean enabled = resultSet.getInt("enabled") == 1;
                    if (_log.isDebugEnabled()) {
                        _log.debug("Creating new user auth object for user {}, authentication is {}", login, enabled ? "enabled" : "disabled");
                    }
                    return new XdatUserAuth(login, XdatUserAuthService.LOCALDB, enabled, true, true, true, AuthorityUtils.NO_AUTHORITIES, login, 0);
                }
            });
            for (XdatUserAuth userAuth : unmapped) {
                if (_log.isDebugEnabled()) {
                    _log.debug("Persisting user auth object for user {}", userAuth.getXdatUsername());
                }
                _xdatUserAuthService.create(userAuth);
            }
            _log.debug("Updating the user auth table to set password updated to the current time for local users");
            _template.execute("UPDATE xhbm_xdat_user_auth SET password_updated=current_timestamp WHERE auth_method='" + XdatUserAuthService.LOCALDB + "' AND password_updated IS NULL");
        } catch (BadSqlGrammarException e) {
            throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(UpdateUserAuthTable.class);

    private final JdbcTemplate _template;
    private final XdatUserAuthService _xdatUserAuthService;
}
