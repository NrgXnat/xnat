package org.nrg.xdat.security.services;

import org.apache.log4j.Logger;
import org.nrg.xft.security.UserI;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class RoleHolder {

    public RoleHolder(){
        try {
            roleService = Class.forName(RoleServiceI.DEFAULT_ROLE_SERVICE).asSubclass(RoleServiceI.class).newInstance();
        }
        catch(Exception e){
            logger.error("",e);
        }
    }

    public RoleHolder(RoleServiceI roleService){
        this.roleService=roleService;
    }

    public void setRoleService(RoleServiceI roleService){
        this.roleService=roleService;
    }

    public boolean checkRole(UserI user, String role) {
        return roleService.checkRole(user,role);
    }

    public void addRole(UserI authenticatedUser, UserI user, String role) throws Exception {
        addRole(authenticatedUser,user,role);
    }

    public void deleteRole(UserI authenticatedUser, UserI user, String role) throws Exception {
        deleteRole(authenticatedUser,user,role);
    }

    public boolean isSiteAdmin(UserI user) {
        return roleService.isSiteAdmin(user);
    }

    public Collection<String> getRoles(UserI user) {
        return roleService.getRoles(user);
    }

    private final static Logger logger = Logger.getLogger(RoleHolder.class);
    private RoleServiceI roleService=null;
}
