/*
 * web: org.nrg.xnat.initialization.tasks.UpdateUserAuthTable
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks whether the site contains any user groups containing underscores. If so, an email is sent to the site admin
 * informing them of the security risk of this.
 */
@SuppressWarnings("SqlDialectInspection")
@Component
@Slf4j
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
        final List<String> groupsWithUnderscores = new ArrayList<>();
        try {
            groupsWithUnderscores.addAll(_template.query("SELECT tag, displayname FROM xdat_usergroup WHERE tag IS NOT NULL AND substr(id, length(tag)+2) LIKE E'%\\\\_%'", new RowMapper<String>() {
                @Override
                public String mapRow(final ResultSet resultSet, final int i) throws SQLException {
                    final String project = resultSet.getString("tag");
                    final String name    = resultSet.getString("displayname");
                    return "group " + name + " in project " + project;
                }
            }));
            log.debug("Ran query to check for groups with underscores in the name, got {} matching groups.", groupsWithUnderscores.size());
        } catch (BadSqlGrammarException e) {
            if (e.getMessage().contains("relation \"xdat_usergroup\" does not exist")) {
                throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization, "Error checking whether groups have underscores", e);
            }
            throw new InitializingTaskException(InitializingTaskException.Level.Error, "A database error occurred while checking whether groups have underscores", e);
        } catch (Exception e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Error, "Error checking whether groups have underscores", e);
        }
        try {
            if (!groupsWithUnderscores.isEmpty()) {
                final String commaSeparatedListOfGroupsWithUnderscores = StringUtils.join(groupsWithUnderscores, ", ");
                AdminUtils.sendAdminEmail("Usergroup security vulnerability", "Your site contains user groups which contain underscores. Users can potentially exploit these to get access to projects they shouldn't have access to. Users are no longer allowed to create such groups, but it is up to you whether to remove or rename groups that were previously created. The groups with underscores currently on the site are: " + commaSeparatedListOfGroupsWithUnderscores);
                log.debug("Sent admin email about {} groups containing underscores in their names: {}", groupsWithUnderscores.size(), commaSeparatedListOfGroupsWithUnderscores);
            }
        } catch (Exception e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Error, "Error sending admin an email about usergroup vulnerability.", e);
        }
    }

    private final JdbcTemplate _template;
}
