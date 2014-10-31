package org.nrg.xdat.security.helpers;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.services.RoleRepositoryServiceI;
import org.nrg.xdat.security.services.RoleRepositoryServiceI.RoleDefinitionI;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xft.security.UserI;

public class Roles {
    static Logger logger = Logger.getLogger(Roles.class);
	private static RoleRepositoryServiceI repository=null;
	private static RoleServiceI roleService=null;
	
	private static RoleRepositoryServiceI getRoleRepositoryService(){
    	if(repository==null){
	       	//default to RoleRepositoryServiceImpl implementation (unless a different default is configured)
    		//we can swap in other ones later by setting a default 
    		//we can even have a config tab in the admin ui which allows sites to select their configuration of choice.
       		try {
				String className=XDAT.getSiteConfigurationProperty("security.roleRepositoryService.default", "org.nrg.xdat.security.services.impl.RoleRepositoryServiceImpl");
				repository=(RoleRepositoryServiceI)Class.forName(className).newInstance();
			} catch (ClassNotFoundException e) {
				logger.error("",e);
			} catch (InstantiationException e) {
				logger.error("",e);
			} catch (IllegalAccessException e) {
				logger.error("",e);
			} catch (ConfigServiceException e) {
				logger.error("",e);
			}
    	}
    	return repository;
	}
	
	public static Collection<RoleDefinitionI> getRoles(){
		return getRoleRepositoryService().getRoles();
	}
	
	private static RoleServiceI getRoleService(){
    	if(roleService==null){
	       	//default to RoleServiceImpl implementation (unless a different default is configured)
    		//we can swap in other ones later by setting a default 
    		//we can even have a config tab in the admin ui which allows sites to select their configuration of choice.
       		try {
				String className=XDAT.getSiteConfigurationProperty("security.roleService.default", "org.nrg.xdat.security.services.impl.RoleServiceImpl");
				roleService=(RoleServiceI)Class.forName(className).newInstance();
			} catch (ClassNotFoundException e) {
				logger.error("",e);
			} catch (InstantiationException e) {
				logger.error("",e);
			} catch (IllegalAccessException e) {
				logger.error("",e);
			} catch (ConfigServiceException e) {
				logger.error("",e);
			}
    	}
    	return roleService;
	}
	
	public static boolean checkRole(UserI user, String role){
		return getRoleService().checkRole(user, role);
	}
	
	public static void deleteRole(UserI authenticatedUser, UserI user, String role) throws Exception{
		getRoleService().deleteRole(authenticatedUser, user, role);
	}
	
	public static void addRole(UserI authenticatedUser, UserI user, String role) throws Exception{
		getRoleService().addRole(authenticatedUser, user, role);
	}
	
	public static boolean isSiteAdmin(UserI user){
		return getRoleService().isSiteAdmin(user);
	}

	public static Collection<String> getRoles(UserI user) {
		return getRoleService().getRoles(user);
	}
}
