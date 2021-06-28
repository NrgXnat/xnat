/*
 * core: org.nrg.xdat.security.helpers.Roles
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.helpers;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.RoleRepositoryHolder;
import org.nrg.xdat.security.services.RoleRepositoryServiceI;
import org.nrg.xdat.security.services.RoleRepositoryServiceI.RoleDefinitionI;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collection;

@SuppressWarnings("ConstantConditions")
@Slf4j
public class Roles {
    public static final String ROLE                     = "role";
    public static final String ROLES                    = "roles";
    public static final String ADDED_ROLES              = "addedRoles";
    public static final String DELETED_ROLES            = "deletedRoles";
    public static final String OPERATION_ADD_ROLE       = "addRole";
    public static final String OPERATION_ADD_ROLES      = "addRoles";
    public static final String OPERATION_DELETE_ROLE    = "deleteRole";
    public static final String OPERATION_DELETE_ROLES   = "deleteRoles";
    public static final String OPERATION_MODIFIED_ROLES = "modifiedRoles";

    public static Collection<RoleDefinitionI> getRoles() {
        return getRoleRepositoryService().getRoles();
    }

    public static boolean checkRole(UserI user, String role) {
        return getRoleService().checkRole(user, role);
    }

    public static boolean deleteRole(UserI authenticatedUser, UserI user, String role) throws Exception {
        return getRoleService().deleteRole(authenticatedUser, user, role);
    }

    public static boolean addRole(UserI authenticatedUser, UserI user, String role) throws Exception {
        return getRoleService().addRole(authenticatedUser, user, role);
    }

    public static boolean isSiteAdmin(UserI user) {
        return getRoleService().isSiteAdmin(user);
    }

    public static boolean isSiteAdmin(final String username) {
        return getRoleService().isSiteAdmin(username);
    }

    public static Collection<String> getRoles(UserI user) {
        return getRoleService().getRoles(user);
    }

    public static RoleRepositoryServiceI getConfiguredRoleRepositoryService() {
        final String className = XDAT.safeSiteConfigProperty("security.roleRepositoryService.default", "org.nrg.xdat.security.services.impl.RoleRepositoryServiceImpl");
        log.info("Building new RoleRepositoryServiceI instance with implementation {}", className);
        try {
            return Class.forName(className).asSubclass(RoleRepositoryServiceI.class).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error("An error occurred trying to instantiate the configured role repository service.", e);
            return null;
        }
    }

    public static RoleServiceI getConfiguredRoleService() {
        final String className = XDAT.safeSiteConfigProperty("security.roleService.default", "org.nrg.xdat.security.services.impl.RoleServiceImpl");
        log.info("Building new RoleServiceI instance with implementation {}", className);
        try {
            return Class.forName(className).asSubclass(RoleServiceI.class).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error("An error occurred trying to instantiate the configured role service.", e);
            return null;
        }
    }

    public static RoleRepositoryHolder getRoleRepositoryService() {
        // MIGRATION: All of these services need to switch from having the implementation in the prefs service to autowiring from the context.

        // First find out if it exists in the application context.
        final ContextService contextService = XDAT.getContextService();
        if (contextService != null) {
            try {
                return contextService.getBean(RoleRepositoryHolder.class);
            } catch (NoSuchBeanDefinitionException ignored) {
                // This is OK, we'll just create it from the indicated class.
            }
        }
        //default to RoleRepositoryServiceImpl implementation (unless a different default is configured)
        //we can swap in other ones later by setting a default
        //we can even have a config tab in the admin ui which allows sites to select their configuration of choice.
        final RoleRepositoryServiceI service = getConfiguredRoleRepositoryService();
        return service != null ? new RoleRepositoryHolder(service) : null;
    }

    public static RoleHolder getRoleService() {
        // First find out if it exists in the application context.
        final ContextService contextService = XDAT.getContextService();
        if (contextService != null) {
            try {
                return contextService.getBean(RoleHolder.class);
            } catch (NoSuchBeanDefinitionException ignored) {
                // This is OK, we'll just create it from the indicated class.
            }
        }
        //default to RoleServiceImpl implementation (unless a different default is configured)
        //we can swap in other ones later by setting a default
        //we can even have a config tab in the admin ui which allows sites to select their configuration of choice.
        final RoleServiceI service = getConfiguredRoleService();
        return service != null ? new RoleHolder(service, XDAT.getContextService().getBean(NamedParameterJdbcTemplate.class)) : null;
    }
}
