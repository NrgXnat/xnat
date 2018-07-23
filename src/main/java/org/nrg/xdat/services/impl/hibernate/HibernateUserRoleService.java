/*
 * core: org.nrg.xdat.services.impl.hibernate.HibernateUserRoleService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2018, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.daos.UserRoleDAO;
import org.nrg.xdat.entities.UserRole;
import org.nrg.xdat.services.UserRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.util.List;

@Service
@Slf4j
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
    @Transactional
    public boolean isUserRole(final String username, final String role) {
        return findUserRole(username, role) != null;
    }

    @Override
    @Transactional
    public UserRole addRoleToUser(final String username, final String role) {
        try {
            if (!isUserRole(username, role)) {
                final UserRole userRole = create(username, role);
                log.debug("Created new role {} for user: {}", role, username);
                return userRole;
            }
        } catch (ConstraintViolationException e) {
            log.warn("Got a constraint violation exception trying to add the role {} to user '{}', in spite of checking that that role didn't already exist.", role, username, e);
        }
        return null;
    }

    @Override
    @Transactional
    public void delete(final String username, final String role) {
        final UserRole userRole = findUserRole(username, role);
        if (userRole != null) {
            log.debug("Deleting user role: {} {}", username, role);
            getDao().delete(userRole);
        }
    }
}
