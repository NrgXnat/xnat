/*
 * core: org.nrg.xdat.security.services.RoleServiceI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.services;

import java.util.Collection;

import org.nrg.xft.security.UserI;

public interface RoleServiceI {
    String DEFAULT_ROLE_SERVICE = "org.nrg.xdat.security.services.impl.RoleServiceImpl";

    boolean checkRole(final UserI user, final String role);

    boolean addRole(final UserI authenticatedUser, final UserI user, final String role) throws Exception;

    boolean deleteRole(final UserI authenticatedUser, final UserI user, final String role) throws Exception;

    Collection<String> getRoles(UserI user);

    boolean isSiteAdmin(final UserI user);

    boolean isSiteAdmin(String username);

    boolean isNonExpiring(final UserI user);
}
