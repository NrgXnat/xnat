package org.nrg.xdat.security.services;

import java.util.Collection;

import org.nrg.xft.security.UserI;

public interface RoleServiceI {
	String DEFAULT_ROLE_SERVICE = "org.nrg.xdat.security.services.impl.RoleServiceImpl";

	boolean checkRole(UserI user, String role);
	void addRole(UserI authenticatedUser, UserI user, String role) throws Exception;
	void deleteRole(UserI authenticatedUser, UserI user, String role) throws Exception;
	boolean isSiteAdmin(UserI user);
	Collection<String> getRoles(UserI user);
}
