package org.nrg.xdat.security.services;

import java.util.Collection;

import org.nrg.xft.security.UserI;

public interface RoleServiceI {
	public boolean checkRole(UserI user, String role);
	public void addRole(UserI authenticatedUser, UserI user, String role) throws Exception;
	public void deleteRole(UserI authenticatedUser, UserI user, String role) throws Exception;
	public boolean isSiteAdmin(UserI user);
	public Collection<String> getRoles(UserI user);
}
