package org.nrg.xdat.security.helpers;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.services.RoleRepositoryServiceI;
import org.nrg.xdat.security.services.RoleRepositoryServiceI.RoleDefinitionI;

public class Roles {
    static Logger logger = Logger.getLogger(Roles.class);
	private static RoleRepositoryServiceI singleton=null;
	
	public static RoleRepositoryServiceI getRoleRepositoryService(){
    	if(singleton==null){
	       	//default to RoleRepositoryServiceImpl implementation (unless a different default is configured)
    		//we can swap in other ones later by setting a default 
    		//we can even have a config tab in the admin ui which allows sites to select their configuration of choice.
       		try {
				String className=XDAT.getSiteConfigurationProperty("security.roleRepositoryService.default", "org.nrg.xdat.security.services.impl.RoleRepositoryServiceImpl");
				singleton=(RoleRepositoryServiceI)Class.forName(className).newInstance();
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
    	return singleton;
	}
	
	public static Collection<RoleDefinitionI> getRoles(){
		return getRoleRepositoryService().getRoles();
	}
}
