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
import org.nrg.xdat.turbine.utils.AdminUtils;
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
 * Checks whether the site contains any user groups containing underscores. If so, an email is sent to the site admin
 * informing them of the security risk of this.
 */
@SuppressWarnings("SqlDialectInspection")
@Component
public class UserGroupSecurityCheck extends AbstractInitializingTask {
    @Autowired
    public UserGroupSecurityCheck(final JdbcTemplate template) {
        super();
        _template = template;
    }

    @Override
    public String getTaskName() {
        return "Inform admin of underscores in groups";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        final List<String> groupsWithUnderscores;
        try {
            groupsWithUnderscores = _template.query("SELECT tag, displayname FROM xdat_usergroup WHERE tag IS NOT NULL AND substr(id, length(tag)+2) LIKE E'%\\\\_%'", new RowMapper<String>() {
                @Override
                public String mapRow(final ResultSet resultSet, final int i) throws SQLException {
                    final String project = resultSet.getString("tag");
                    final String name = resultSet.getString("displayname");
                    return "group "+name+" in project "+project;
                }
            });

        } catch (Exception e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Error, "Error checking whether groups have underscores", e);
        }
        try {
            if(groupsWithUnderscores!=null && groupsWithUnderscores.size()>0) {
                String commaSeparatedListOfGroupsWithUnderscores = "";
                for (String groupText : groupsWithUnderscores) {
                    commaSeparatedListOfGroupsWithUnderscores = commaSeparatedListOfGroupsWithUnderscores+groupText+", ";
                }
                commaSeparatedListOfGroupsWithUnderscores = commaSeparatedListOfGroupsWithUnderscores.substring(0, commaSeparatedListOfGroupsWithUnderscores.length()-2);
                AdminUtils.sendAdminEmail("Usergroup security vulnerability", "Your site contains usergroups which contain underscores. Users can potentially exploit these to get access to projects they shouldn't have access to. Users are no longer allowed to create such groups, but it is up to you whether to remove or rename groups that were previously created. The groups with underscores currently on the site are: "+commaSeparatedListOfGroupsWithUnderscores+".");
            }
        } catch (Exception e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Error, "Error sending admin an email about usergroup vulnerability.", e);
        }

    }

    private static final Logger _log = LoggerFactory.getLogger(UserGroupSecurityCheck.class);

    private final JdbcTemplate _template;
}
