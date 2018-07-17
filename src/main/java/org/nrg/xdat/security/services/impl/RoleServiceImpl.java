/*
 * core: org.nrg.xdat.security.services.impl.RoleServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.services.impl;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
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
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class RoleServiceImpl implements RoleServiceI {
    @Autowired
    public RoleServiceImpl(final UserRoleService service, final NamedParameterJdbcTemplate template) {
        _service = service;
        _template = template;
    }

    @Override
    public boolean checkRole(final UserI user, final String role) {
        return hasUsernameAndRole(user.getUsername(), role);
    }

    @Override
    public void addRole(final UserI authenticatedUser, final UserI user, final String role) throws Exception {
        ((XDATUser) user).addRole(authenticatedUser, role);
    }

    @Override
    public void deleteRole(final UserI authenticatedUser, final UserI user, final String role) throws Exception {
        ((XDATUser) user).deleteRole(authenticatedUser, role);
    }

    @Override
    public Collection<String> getRoles(final UserI user) {
        final Set<String> roles = new HashSet<>();
        roles.addAll(Lists.transform(_service.findRolesForUser(user.getUsername()), new Function<UserRole, String>() {
            @Override
            public String apply(final UserRole userRole) {
                return userRole.getRole();
            }
        }));
        roles.addAll(_template.queryForList(QUERY_ROLES_FOR_USER, new MapSqlParameterSource("username", user.getUsername()), String.class));
        return roles;
    }

    @Override
    public boolean isSiteAdmin(final UserI user) {
        return hasUsernameAndRole(user.getUsername(), UserRole.ROLE_ADMINISTRATOR);
    }

    @Override
    public boolean isNonExpiring(final UserI user) {
        return hasUsernameAndRole(user.getUsername(), UserRole.ROLE_NON_EXPIRING);
    }

    private boolean hasUsernameAndRole(final String username, final String role) {
        return _service.exists("username", username, "role", role) || _template.queryForObject(QUERY_USER_HAS_ROLE, new MapSqlParameterSource("username", username).addValue("role", role), Boolean.class);
    }

    private static final String QUERY_ROLES_FOR_USER = "SELECT " +
                                                       "  xrt.role_name " +
                                                       "FROM xdat_user u " +
                                                       "  LEFT JOIN xdat_r_xdat_role_type_assign_xdat_user u2r ON u.xdat_user_id = u2r.xdat_user_xdat_user_id " +
                                                       "  LEFT JOIN xdat_role_type xrt on u2r.xdat_role_type_role_name = xrt.role_name " +
                                                       "WHERE" +
                                                       "  u.login = :username";
    private static final String QUERY_USER_HAS_ROLE  = "SELECT EXISTS(" + QUERY_ROLES_FOR_USER + " AND xrt.role_name = :role)";

    private final UserRoleService            _service;
    private final NamedParameterJdbcTemplate _template;
}
