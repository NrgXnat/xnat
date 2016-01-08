package org.nrg.xdat.security.services.impl;

import org.apache.log4j.Logger;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xft.security.UserI;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class RoleServiceImpl implements RoleServiceI {
    @Override
    public boolean checkRole(UserI user, String role) {
        try {
            return ((XDATUser) user).checkRole(role);
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }
    }

    @Override
    public void addRole(UserI authenticatedUser, UserI user, String role) throws Exception {
        ((XDATUser) user).addRole(authenticatedUser, role);
    }

    @Override
    public void deleteRole(UserI authenticatedUser, UserI user, String role) throws Exception {
        ((XDATUser) user).deleteRole(authenticatedUser, role);
    }

    @Override
    public boolean isSiteAdmin(UserI user) {
        return ((XDATUser) user).isSiteAdmin();
    }

    @Override
    public Collection<String> getRoles(UserI user) {
        return ((XDATUser) user).getRoleNames();
    }

    private final static Logger logger = Logger.getLogger(RoleServiceImpl.class);
}
