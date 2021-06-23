/*
 * core: org.nrg.xdat.security.services.RoleHolder
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.services;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleHolder {
    /**
     * Creates the class with the specified instance of the {@link RoleServiceI role service} and JDBC template.
     *
     * @param roleService The role service to use for XFT operations.
     * @param template    The JDBC template to use for database operations.
     */
    @Autowired
    public RoleHolder(final RoleServiceI roleService, final NamedParameterJdbcTemplate template) {
        _roleService = roleService;
        _template = template;
    }

    /**
     * Sets the role service to use for XFT operations.
     *
     * @param roleService The role service to use for XFT operations.
     */
    public void setRoleService(final RoleServiceI roleService) {
        _roleService = roleService;
    }

    /**
     * Checks whether the specified user is assigned the specified role.
     *
     * @param user The user you want to check for the specified role.
     * @param role The role you want to check for.
     *
     * @return Returns true if the user has the specified role, false otherwise.
     */
    public boolean checkRole(final UserI user, final String role) {
        return _roleService.checkRole(user, role);
    }

    /**
     * Adds the specified role to the user.
     *
     * @param authenticatedUser The user who is assigning the specified role to the user.
     * @param user              The user to which you want to assign the specified role.
     * @param role              The role you want to assign to the user.
     *
     * @return Returns true if the role was assigned to the user, false otherwise.
     */
    public boolean addRole(final UserI authenticatedUser, final UserI user, final String role) throws Exception {
        return _roleService.addRole(authenticatedUser, user, role);
    }

    /**
     * Deletes the specified role from the user.
     *
     * @param authenticatedUser The user who is deleting the specified role from the user.
     * @param user              The user to which you want to delete the specified role.
     * @param role              The role you want to delete from the user.
     *
     * @return Returns true if the role was deleted from the user, false otherwise.
     */
    public boolean deleteRole(final UserI authenticatedUser, final UserI user, final String role) throws Exception {
        return _roleService.deleteRole(authenticatedUser, user, role);
    }

    /**
     * Checks whether the specified user is assigned the administrator role.
     *
     * @param user The user you want to check for the administrator role.
     *
     * @return Returns true if the user has the administrator role, false otherwise.
     */
    public boolean isSiteAdmin(final UserI user) {
        return _roleService.isSiteAdmin(user);
    }

    /**
     * Checks whether the specified user is assigned the administrator role.
     *
     * @param username The name of the user you want to check for the administrator role.
     *
     * @return Returns true if the user has the administrator role, false otherwise.
     */
    public boolean isSiteAdmin(final String username) {
        return _roleService.isSiteAdmin(username);
    }

    /**
     * Gets all of the roles on the system.
     *
     * @return A list of roles on the system.
     */
    public Collection<String> getRoles() {
        return _template.queryForList(QUERY_ALL_ROLES, EmptySqlParameterSource.INSTANCE, String.class);
    }

    /**
     * Gets all of the roles that are assigned to the specified user.
     *
     * @return A list of roles assigned to the specified user.
     */
    public Collection<String> getRoles(final UserI user) {
        return _roleService.getRoles(user);
    }

    /**
     * Gets the names of all of the users that have the specified role.
     *
     * @return A list of names of users that have the specified role.
     */
    public Collection<String> getUsers(final String role) {
        return _roleService.getUsers(role);
    }

    /**
     * Gets a map of roles with the users that have each role.
     *
     * @return A map of roles and usernames.
     */
    public Map<String, Collection<String>> getRolesAndUsers() {
        return _template.queryForList(QUERY_ALL_ROLES, EmptySqlParameterSource.INSTANCE, String.class).stream().collect(Collectors.toMap(Function.identity(), this::getUsers));
    }

    /**
     * Gets a map of project-based roles for the specified user.
     *
     * @param user The user for which you want to retrieve project-based roles.
     *
     * @return A list of maps of project-based roles for the specified user.
     */
    public Collection<Map<String, String>> getUserProjectRoles(final UserI user) {
        return getUserProjectRoles(user.getUsername());
    }

    /**
     * Gets a map of project-based roles for the specified user.
     *
     * @param username The name of tje user for which you want to retrieve project-based roles.
     *
     * @return A list of maps of project-based roles for the specified user.
     */
    public Collection<Map<String, String>> getUserProjectRoles(final String username) {
        return _template.query(QUERY_USER_PROJECT_ROLES, new MapSqlParameterSource("username", username), USER_PROJECT_ROLES_MAPPER);
    }

    private static final RowMapper<Map<String, String>> USER_PROJECT_ROLES_MAPPER = (resultSet, index) -> {
        final Map<String, String> properties = new HashMap<>();
        properties.put("secondary_ID", resultSet.getString("secondary_ID"));
        properties.put("ID", resultSet.getString("ID"));
        properties.put("role", resultSet.getString("role"));
        properties.put("name", resultSet.getString("name"));
        properties.put("URI", resultSet.getString("URI"));
        properties.put("group_id", resultSet.getString("group_id"));
        properties.put("description", resultSet.getString("description"));
        return properties;
    };

    // TODO: This doesn't include all-data access and admin "roles", which are really groups. See comment on XNAT-6769
    //  for more info.
    private static final String QUERY_ALL_ROLES          = "WITH " +
                                                           "    all_roles AS ( " +
                                                           "        SELECT " +
                                                           "            role " +
                                                           "        FROM " +
                                                           "            xhbm_user_role " +
                                                           "        UNION " +
                                                           "        SELECT " +
                                                           "            role_name AS role " +
                                                           "        FROM " +
                                                           "            xdat_role_type) " +
                                                           "SELECT DISTINCT " +
                                                           "    role " +
                                                           "FROM " +
                                                           "    all_roles";
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

    private final NamedParameterJdbcTemplate _template;

    private RoleServiceI _roleService;
}
