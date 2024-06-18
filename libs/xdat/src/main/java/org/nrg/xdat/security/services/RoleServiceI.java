/*
 * core: org.nrg.xdat.security.services.RoleServiceI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.services;

import java.util.Collection;

import org.nrg.xft.security.UserI;

public interface RoleServiceI {
    String DEFAULT_ROLE_SERVICE = "org.nrg.xdat.security.services.impl.RoleServiceImpl";

    /**
     * Checks whether the specified user is assigned the specified role.
     *
     * @param user The user you want to check for the specified role.
     * @param role The role you want to check for.
     *
     * @return Returns true if the user has the specified role, false otherwise.
     */
    boolean checkRole(final UserI user, final String role);

    /**
     * Adds the specified role to the user.
     *
     * @param authenticatedUser The user who is assigning the specified role to the user.
     * @param user              The user to which you want to assign the specified role.
     * @param role              The role you want to assign to the user.
     *
     * @return Returns true if the role was assigned to the user, false otherwise.
     */
    boolean addRole(final UserI authenticatedUser, final UserI user, final String role) throws Exception;

    /**
     * Deletes the specified role from the user.
     *
     * @param authenticatedUser The user who is deleting the specified role from the user.
     * @param user              The user to which you want to delete the specified role.
     * @param role              The role you want to delete from the user.
     *
     * @return Returns true if the role was deleted from the user, false otherwise.
     */
    boolean deleteRole(final UserI authenticatedUser, final UserI user, final String role) throws Exception;

    /**
     * Gets all of the roles assigned to the specified user.
     *
     * @param user The user for which you want to retrieve a list of roles.
     *
     * @return The roles assigned to the specified user.
     */
    Collection<String> getRoles(final UserI user);

    /**
     * Gets the names of all of the users with the specified role.
     *
     * @param role The role for which you want to retrieve a list of users.
     *
     * @return The users with the specified role.
     */
    Collection<String> getUsers(final String role);

    /**
     * Checks whether the specified user is assigned the administrator role.
     *
     * @param user The user you want to check for the administrator role.
     *
     * @return Returns true if the user has the administrator role, false otherwise.
     */
    boolean isSiteAdmin(final UserI user);

    /**
     * Checks whether the specified user is assigned the administrator role.
     *
     * @param username The name of the user you want to check for the administrator role.
     *
     * @return Returns true if the user has the administrator role, false otherwise.
     */
    boolean isSiteAdmin(final String username);

    /**
     * Checks whether the specified user is assigned the non-expiring role.
     *
     * @param user The user you want to check for the non-expiring role.
     *
     * @return Returns true if the user has the non-expiring role, false otherwise.
     */
    @SuppressWarnings("unused")
    boolean isNonExpiring(final UserI user);
}
