/*
 * core: org.nrg.xdat.security.services.RoleHolder
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.services;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class RoleHolder {
    public RoleHolder() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        _roleService = Class.forName(RoleServiceI.DEFAULT_ROLE_SERVICE).asSubclass(RoleServiceI.class).newInstance();
    }

    public RoleHolder(final RoleServiceI roleService) {
        _roleService = roleService;
    }

    public RoleHolder(final RoleServiceI roleService, final NamedParameterJdbcTemplate template) {
        _roleService = roleService;
        _template = template;
    }

    @Autowired
    public void setNamedParameterJdbcTemplate(final NamedParameterJdbcTemplate template) {
        _template = template;
    }

    public void setRoleService(final RoleServiceI roleService) {
        _roleService = roleService;
    }

    public boolean checkRole(final UserI user, final String role) {
        return _roleService.checkRole(user, role);
    }

    public void addRole(final UserI authenticatedUser, final UserI user, final String role) throws Exception {
        _roleService.addRole(authenticatedUser, user, role);
    }

    public void deleteRole(final UserI authenticatedUser, final UserI user, final String role) throws Exception {
        _roleService.deleteRole(authenticatedUser, user, role);
    }

    public boolean isSiteAdmin(final UserI user) {
        return _roleService.isSiteAdmin(user);
    }

    public Collection<String> getRoles(final UserI user) {
        return _roleService.getRoles(user);
    }

    public Collection<Map<String, String>> getUserProjectRoles(final UserI user) {
        return getUserProjectRoles(user.getUsername());
    }

    public Collection<Map<String, String>> getUserProjectRoles(final String username) {
        return _template.query(QUERY_USER_PROJECT_ROLES, new MapSqlParameterSource("username", username), USER_PROJECT_ROLES_MAPPER);
    }

    private static final RowMapper<Map<String, String>> USER_PROJECT_ROLES_MAPPER = new RowMapper<Map<String, String>>() {
        @Override
        public Map<String, String> mapRow(final ResultSet resultSet, final int index) throws SQLException {
            final Map<String, String> properties = new HashMap<>();
            properties.put("secondary_ID", resultSet.getString("secondary_ID"));
            properties.put("ID", resultSet.getString("ID"));
            properties.put("role", resultSet.getString("role"));
            properties.put("name", resultSet.getString("name"));
            properties.put("URI", resultSet.getString("URI"));
            properties.put("group_id", resultSet.getString("group_id"));
            properties.put("description", resultSet.getString("description"));
            return properties;
        }
    };

    private static final String QUERY_USER_PROJECT_ROLES = "SELECT " +
                                                           "  project.secondary_id AS secondary_ID, " +
                                                           "  project.id AS ID, " +
                                                           "  usergroup.displayname AS role, " +
                                                           "  project.name AS name, " +
                                                           "  '/data/projects/' || project.id AS URI, " +
                                                           "  usergroup.id AS group_id, " +
                                                           "  coalesce(project.description, '') AS description " +
                                                           "FROM xdat_user u " +
                                                           "  LEFT JOIN xdat_user_groupid map ON u.xdat_user_id = map.groups_groupid_xdat_user_xdat_user_id " +
                                                           "  LEFT JOIN xdat_usergroup usergroup on map.groupid = usergroup.id " +
                                                           "  LEFT JOIN xnat_projectdata project ON usergroup.tag = project.id " +
                                                           "WHERE " +
                                                           "  usergroup.id IS NOT NULL AND " +
                                                           "  u.login = :username " +
                                                           "ORDER BY secondary_ID";

    private RoleServiceI               _roleService;
    private NamedParameterJdbcTemplate _template;
}
