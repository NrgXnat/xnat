package org.nrg.xdat.security.services.impl;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xft.security.UserI;

public class RoleServiceImpl implements RoleServiceI {
    static Logger logger = Logger.getLogger(RoleServiceImpl.class);

	@Override
	public boolean checkRole(UserI user, String role) {
		try {
			return ((XDATUser)user).checkRole(role);
		} catch (Exception e) {
			logger.error("",e);
			return false;
		}
	}

	@Override
	public void addRole(UserI authenticatedUser, UserI user, String role) throws Exception {
		((XDATUser)user).addRole(authenticatedUser, role);
	}

	@Override
	public void deleteRole(UserI authenticatedUser, UserI user, String role) throws Exception {
		((XDATUser)user).deleteRole(authenticatedUser, role);
	}

	@Override
	public boolean isSiteAdmin(UserI user) {
		return ((XDATUser)user).isSiteAdmin();
	}

	@Override
	public Collection<String> getRoles(UserI user) {
		return ((XDATUser)user).getRoleNames();
	}

}
