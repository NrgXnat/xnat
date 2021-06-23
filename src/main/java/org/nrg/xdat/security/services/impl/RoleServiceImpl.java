/*
 * core: org.nrg.xdat.security.services.impl.RoleServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.entities.UserRole;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.services.UserRoleService;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class RoleServiceImpl implements RoleServiceI {
    @Autowired
    public RoleServiceImpl(final UserRoleService service, final NamedParameterJdbcTemplate template) {
        _service = service;
        _template = template;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkRole(final UserI user, final String role) {
        return hasUsernameAndRole(user.getUsername(), role);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addRole(final UserI authenticatedUser, final UserI user, final String role) throws Exception {
        return ((XDATUser) user).addRole(authenticatedUser, role);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteRole(final UserI authenticatedUser, final UserI user, final String role) throws Exception {
        return ((XDATUser) user).deleteRole(authenticatedUser, role);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getRoles(final UserI user) {
        return Stream.concat(_service.findRolesForUser(user.getUsername()).stream().map(UserRole::getRole), _template.queryForList(QUERY_ROLES_FOR_USER, new MapSqlParameterSource("username", user.getUsername()), String.class).stream()).collect(Collectors.toSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getUsers(final String role) {
        return Stream.concat(_service.findUsersForRole(role).stream().map(UserRole::getUsername), _template.queryForList(QUERY_USERS_FOR_ROLE, new MapSqlParameterSource("role", role), String.class).stream()).collect(Collectors.toSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSiteAdmin(final UserI user) {
        return isSiteAdmin(user.getUsername());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSiteAdmin(final String username) {
        return hasUsernameAndRole(username, UserRole.ROLE_ADMINISTRATOR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNonExpiring(final UserI user) {
        return hasUsernameAndRole(user.getUsername(), UserRole.ROLE_NON_EXPIRING);
    }

    /**
     * Checks whether the specified username is assigned the indicated role.
     *
     * @param username The username to check.
     * @param role     The role to check.
     *
     * @return Returns true if the user is assigned to the specified role, false otherwise.
     */
    private boolean hasUsernameAndRole(final String username, final String role) {
        return _service.exists("username", username, "role", role) || _template.queryForObject(QUERY_USER_HAS_ROLE, new MapSqlParameterSource("username", username).addValue("role", role), Boolean.class);
    }

    private static final String QUERY_ROLES_FOR_USER = "SELECT " +
                                                       "  xrt.role_name " +
                                                       "FROM xdat_user u " +
                                                       "  LEFT JOIN xdat_r_xdat_role_type_assign_xdat_user u2r ON u.xdat_user_id = u2r.xdat_user_xdat_user_id " +
                                                       "  LEFT JOIN xdat_role_type xrt ON u2r.xdat_role_type_role_name = xrt.role_name " +
                                                       "WHERE " +
                                                       "  xrt.role_name IS NOT NULL AND " +
                                                       "  u.login = :username";
    private static final String QUERY_USERS_FOR_ROLE = "SELECT " +
                                                       "    u.login " +
                                                       "FROM " +
                                                       "    xdat_role_type r " +
                                                       "        LEFT JOIN xdat_r_xdat_role_type_assign_xdat_user u2r ON r.role_name = u2r.xdat_role_type_role_name " +
                                                       "        LEFT JOIN xdat_user u ON u2r.xdat_user_xdat_user_id = u.xdat_user_id " +
                                                       "WHERE " +
                                                       "    r.role_name = :role";
    private static final String QUERY_USER_HAS_ROLE  = "SELECT EXISTS(" + QUERY_ROLES_FOR_USER + " AND xrt.role_name = :role)";

    private final UserRoleService            _service;
    private final NamedParameterJdbcTemplate _template;
}
