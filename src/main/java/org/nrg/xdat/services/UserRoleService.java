/**
 * UserRoleService
 * (C) 2013 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 6/20/13 by Tim Olsen
 */
package org.nrg.xdat.services;

import java.util.List;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xdat.entities.UserRole;

public interface UserRoleService extends BaseHibernateService<UserRole> {
    /**
     * Finds all roles for the specified user
     * @param username    The username from the XdatUser table.
     * @return An list of the {@link UserRole user roles} issued to the indicated user.
     */
    abstract public List<UserRole> findRolesForUser(String username);
    /**
     * Finds all users for the specified role.
     * @param role    The role to match.
     * @return An list of the {@link UserRole user roles} issued to the indicated role.
     */
    abstract public List<UserRole> findUsersForRole(String role);

    /**
     * Deletes the specified user role combo.
     * @param username    The username to match.
     * @param role    The role to match.
     * @return .
     */
    abstract public void delete(final String username, final String role);

    /**
     * Creates the specified user role combo.
     * @param username    The username.
     * @param role    The role.
     * @return created UserRole
     */
    abstract public UserRole addRoleToUser(final String username, final String role);
    
    /**
     * Finds all matching user roles
     * @param username    The username.
     * @param role    The role.
     * @return The matched user role
     */
    abstract public UserRole findUserRole(String username,String role);
}
