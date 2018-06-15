/*
 * core: org.nrg.xdat.security.helpers.Roles
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
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

@Slf4j
public class Roles {
    private static RoleRepositoryHolder getRoleRepositoryService() {
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
        try {
            String className = XDAT.safeSiteConfigProperty("security.roleRepositoryService.default", "org.nrg.xdat.security.services.impl.RoleRepositoryServiceImpl");
            return new RoleRepositoryHolder((RoleRepositoryServiceI) Class.forName(className).newInstance());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error("An error occurred trying to instantiate the configured role repository service.", e);
        }
        return null;
    }

    public static Collection<RoleDefinitionI> getRoles() {
        return getRoleRepositoryService().getRoles();
    }

    private static RoleHolder getRoleService() {
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
        try {
            final String className = XDAT.safeSiteConfigProperty("security.roleService.default", "org.nrg.xdat.security.services.impl.RoleServiceImpl");
            final NamedParameterJdbcTemplate template = XDAT.getContextService().getBean(NamedParameterJdbcTemplate.class);
            return new RoleHolder((RoleServiceI) Class.forName(className).newInstance(), template);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            log.error("An error occurred trying to instantiate the configured role service.", e);
        }

        return null;
    }

    public static boolean checkRole(UserI user, String role) {
        return getRoleService().checkRole(user, role);
    }

    public static void deleteRole(UserI authenticatedUser, UserI user, String role) throws Exception {
        getRoleService().deleteRole(authenticatedUser, user, role);
    }

    public static void addRole(UserI authenticatedUser, UserI user, String role) throws Exception {
        getRoleService().addRole(authenticatedUser, user, role);
    }

    public static boolean isSiteAdmin(UserI user) {
        return getRoleService().isSiteAdmin(user);
    }

    public static Collection<String> getRoles(UserI user) {
        return getRoleService().getRoles(user);
    }
}
