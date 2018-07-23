/*
 * core: org.nrg.xdat.services.impl.hibernate.HibernateUserRoleService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2018, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services.impl.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.daos.UserRoleDAO;
import org.nrg.xdat.entities.UserRole;
import org.nrg.xdat.services.UserRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HibernateUserRoleService extends AbstractHibernateEntityService<UserRole, UserRoleDAO> implements UserRoleService {
    /**
     * Finds all roles for the specified user
     * @param username    The username from the XdatUser table.
     * @return An list of the {@link UserRole user roles} issued to the indicated user.
     */
    @Override
    @Transactional
    public List<UserRole> findRolesForUser(final String username){
    	return getDao().findByUser(username);
    }
    /**
     * Finds all users for the specified role.
     * @param role    The role to match.
     * @return An list of the {@link UserRole user roles} issued to the indicated role.
     */
    @Override
    @Transactional
    public List<UserRole> findUsersForRole(final String role){
    	return getDao().findByRole(role);
    }

    /**
     * Finds all users for the specified role.
     * @param role    The role to match.
     * @return An list of the {@link UserRole user roles} issued to the indicated role.
     */
    @Override
    @Transactional
    public UserRole findUserRole(final String username, final String role){
    	return getDao().findByUserRole(username, role);
    }

    @Override
    public boolean isUserRole(final String username, final String role) {
        return false;
    }

    @Override
    @Transactional
    public UserRole addRoleToUser(final String username, final String role) {
        UserRole token = newEntity();
        token.setUsername(username);
        token.setRole(role);
        getDao().create(token);
        if (_log.isDebugEnabled()) {
            _log.debug("Created new role " + token.getRole() + " for user: " + token.getUsername());
        }
        return token;
    }

    @Override
    @Transactional
    public void delete(final String username, final String role) {
        UserRole ur = getDao().findByUserRole(username, role);
        if (ur != null) {
            if (_log.isDebugEnabled()) {
                _log.debug("Deleting user role: " + username + " " + role);
            }
            getDao().delete(ur);
        }
    }

    private static final Log _log = LogFactory.getLog(HibernateUserRoleService.class);


}
