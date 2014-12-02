package org.nrg.xdat.security.helpers;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.services.SearchHelperServiceI;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xft.security.UserI;

public class UserHelper {
    static Logger logger = Logger.getLogger(UserHelper.class);
    
    
    private static SearchHelperServiceI search_singleton=null;
    /**
     * Returns the currently configured permissions service
     * 
     * You can customize the implementation returned by adding a new implementation to the org.nrg.xdat.security.user.custom package (or a diffently configured package).
     * 
     * You can change the default implementation returned via the security.userManagementService.default configuration parameter
     * @return
     */
    public static SearchHelperServiceI getSearchHelperService(){
    	if(search_singleton==null){
    		 try {
				List<Class<?>> classes = Reflection.getClassesForPackage(XDAT.safeSiteConfigProperty("security.searchHelperService.package", "org.nrg.xdat.search.helper.custom"));

				 if(classes!=null && classes.size()>0){
					 for(Class<?> clazz: classes){
						 if(SearchHelperServiceI.class.isAssignableFrom(clazz)){
							search_singleton=(SearchHelperServiceI)clazz.newInstance();
						 }
					 }
				 }
			} catch (ClassNotFoundException e) {
				logger.error("",e);
			} catch (InstantiationException e) {
				logger.error("",e);
			} catch (IllegalAccessException e) {
				logger.error("",e);
			} catch (IOException e) {
				logger.error("",e);
			}
	       	 
	       	 //default to XDATUserHelperService implementation (unless a different default is configured)
	       	 if(search_singleton==null){
	       		try {
					String className=XDAT.safeSiteConfigProperty("security.searchHelperService.default", "org.nrg.xdat.security.XDATSearchHelperService");
					search_singleton=(SearchHelperServiceI)Class.forName(className).newInstance();
				} catch (ClassNotFoundException e) {
					logger.error("",e);
				} catch (InstantiationException e) {
					logger.error("",e);
				} catch (IllegalAccessException e) {
					logger.error("",e);
				}
	       	 }
    	}
    	return search_singleton;
    }
    
    

    private static Class<UserHelperServiceI> userHelperImpl=null;
    /**
     * Returns the currently configured permissions service
     * 
     * You can customize the implementation returned by adding a new implementation to the org.nrg.xdat.security.user.custom package (or a diffently configured package).
     * 
     * You can change the default implementation returned via the security.userManagementService.default configuration parameter
     * @return
     */
    private static Class<UserHelperServiceI> getUserHelperImpl(){
    	if(userHelperImpl==null){
    		 try {
				List<Class<?>> classes = Reflection.getClassesForPackage(XDAT.safeSiteConfigProperty("security.userHelperService.package", "org.nrg.xdat.user.helper.custom"));

				 if(classes!=null && classes.size()>0){
					 for(Class<?> clazz: classes){
						 if(UserHelperServiceI.class.isAssignableFrom(clazz)){
							 userHelperImpl=(Class<UserHelperServiceI>)clazz;
						 }
					 }
				 }
			} catch (ClassNotFoundException e) {
				logger.error("",e);
			} catch (IOException e) {
				logger.error("",e);
			}
	       	 
	       	 //default to XDATUserHelperService implementation (unless a different default is configured)
	       	 if(search_singleton==null){
	       		try {
					String className=XDAT.safeSiteConfigProperty("security.userHelperService.default", "org.nrg.xdat.security.XDATUserHelperService");
					userHelperImpl=(Class<UserHelperServiceI>)Class.forName(className);
				} catch (ClassNotFoundException e) {
					logger.error("",e);
				}
	       	 }
    	}
    	return userHelperImpl;
    }
    
    public static UserHelperServiceI getUserHelperService(UserI user){
    	try {
			Class<UserHelperServiceI> clazz =getUserHelperImpl();
			UserHelperServiceI serv= (UserHelperServiceI)clazz.newInstance();
			serv.setUser(user);
			return serv;
		} catch (InstantiationException e) {
			logger.error("",e);
			return null;
		} catch (IllegalAccessException e) {
			logger.error("",e);
			return null;
		}
    }
}
