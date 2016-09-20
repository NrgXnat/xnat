/*
 * core: org.nrg.xdat.security.Authorizer
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;

public class Authorizer implements AuthorizerI{
	static Logger logger = Logger.getLogger(Authorizer.class);

	private Authorizer(){}
	
	public static AuthorizerI getInstance(){
		return new Authorizer();
	}

	/* (non-Javadoc)
	 * User is authorized if:
	 *  the datatype is specifically marked as unsecured
	 *  the user is an administrator
	 *  or the data type is otherwise secured.
	 * @see org.nrg.xdat.security.AuthorizerI#authorizeSave(org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement, org.nrg.xft.security.UserI)
	 * 
	 */
	public void authorizeRead(final XFTItem e, final UserI user) throws Exception {
		authorize(SecurityManager.READ,e,user);
	}

	private static final Map<String,List<String>> protectedNamespace= 
		new HashMap<String,List<String>>(){{
			put(SecurityManager.READ,Arrays.asList(new String[]{XFT.PREFIX}));
			put(SecurityManager.EDIT,Arrays.asList(new String[]{XFT.PREFIX,"arc"}));
			put(SecurityManager.DELETE,Arrays.asList(new String[]{XFT.PREFIX,"arc"}));
		}};

	private static final Map<String,List<String>> unsecured= 
		new HashMap<String,List<String>>(){{
			put(SecurityManager.READ,Arrays.asList("wrk:workflowData",XdatStoredSearch.SCHEMA_ELEMENT_NAME,"xnat:fieldDefinitionGroup","xnat:investigatorData"));
			put(SecurityManager.EDIT,Arrays.asList("wrk:workflowData",XdatStoredSearch.SCHEMA_ELEMENT_NAME,"xnat:fieldDefinitionGroup","xnat:investigatorData"));
			put(SecurityManager.DELETE,Arrays.asList("wrk:workflowData",XdatStoredSearch.SCHEMA_ELEMENT_NAME,"xnat:fieldDefinitionGroup"));
		}};

	
	/* (non-Javadoc)
	 * User is authorized if:
	 *  the datatype is specificalloy marked as unsecured
	 *  the user is an administrator
	 *  or the data type is otherwise secured.
	 * @see org.nrg.xdat.security.AuthorizerI#authorizeSave(org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement, org.nrg.xft.security.UserI)
	 * 
	 */
	public void authorizeSave(final XFTItem e, final UserI user) throws Exception{
		authorize(SecurityManager.EDIT,e,user);
	}
	
	static boolean has_users=false;
	private static synchronized boolean hasUsers() throws SQLException, Exception{
		if(!has_users){
			Long user_count=(Long)PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) AS USER_COUNT FROM xdat_user;", "USER_COUNT", PoolDBUtils.getDefaultDBName(), null);
			if(user_count>0){
				has_users=true;
			}
		}
		
		return has_users;
	}
	
	private boolean requiresSecurity(String action,final GenericWrapperElement e, final UserI user) throws SQLException, Exception{
		if(user != null){
			return (!unsecured.get(action).contains(e.getXSIType()) && ((user==null && hasUsers()) || !Roles.isSiteAdmin(user)));
		} else {
			return (!unsecured.get(action).contains(e.getXSIType()) && ((user==null && hasUsers())));
		}
	}
	
	public void authorize(String action,final GenericWrapperElement e, final UserI user) throws Exception{
		if (requiresSecurity(action,e,user))
        {
            if (user.isGuest() && !action.equalsIgnoreCase(SecurityManager.READ)) {
                throwException(new InvalidPermissionException("Guest users can not perform the requested operation " + action + " on the element type " + e.getXSIType()));
            }

            if(protectedNamespace.get(action).contains(e.getType().getForeignPrefix())){
	    		AdminUtils.sendAdminEmail(user,"Unauthorized Admin Data Access Attempt", "Unauthorized access of '" + e.getXSIType() + "' by '" + ((user==null)?null:user.getUsername()) + "' prevented.");
	    		throwException(new InvalidPermissionException("Only site administrators can read core documents."));
	        }else if(!ElementSecurity.IsSecureElement(e.getXSIType())){
	        	AdminUtils.sendAdminEmail(user,"Unauthorized Data Access Attempt", "Unauthorized access of '" + e.getXSIType() + "' by '" + ((user==null)?null:user.getUsername()) + "' prevented.");
	        	throwException(new InvalidPermissionException("Unsecured data access attempt"));
	        }
        }
	}
	
	public void authorize(String action,final XFTItem item, final UserI user) throws Exception{
		if (requiresSecurity(action,item.getGenericSchemaElement(),user))
        {
			authorize(action,item.getGenericSchemaElement(),user);
			if(ElementSecurity.IsSecureElement(item.getXSIType())){
	        	if(!Permissions.can(user,item,action)){
	        		AdminUtils.sendAdminEmail(user,"Unauthorized Data Retrieval Attempt", "Unauthorized access of '" + item.getXSIType() + "' by '" + ((user==null)?null:user.getUsername()) + "' prevented.");
		    		throwException(new InvalidPermissionException("Unsecured data Access attempt"));
		        }
	        }
        }
	}
	
	public static void throwException(Exception e) throws Exception{
		logger.error("",e);
		throw e;
	}
	

	/* (non-Javadoc)
	 * User is authorized if:
	 *  the datatype is specificalloy marked as unsecured
	 *  the user is an administrator
	 *  or the data type is otherwise secured.
	 * @see org.nrg.xdat.security.AuthorizerI#authorizeSave(org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement, org.nrg.xft.security.UserI)
	 * 
	 */
	public void authorizeDelete(final XFTItem e, final UserI user) throws Exception{
		authorize(SecurityManager.DELETE,e,user);
	}

	public void authorizeRead(GenericWrapperElement e, UserI user)
			throws Exception {
		authorize(SecurityManager.READ,e,user);
	}

	public void authorizeSave(GenericWrapperElement e, UserI user)
			throws Exception {
		authorize(SecurityManager.READ,e,user);
	}

	public void authorizeDelete(GenericWrapperElement e, UserI user)
			throws Exception {
		authorize(SecurityManager.DELETE,e,user);
	}
	
}
