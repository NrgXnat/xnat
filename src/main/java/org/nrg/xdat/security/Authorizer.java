/*
 * core: org.nrg.xdat.security.Authorizer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import org.apache.log4j.Logger;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;

import java.util.*;

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

	private static final List<String> XFT_ARRAY          = Collections.singletonList(XFT.PREFIX);
	private static final List<String> XFT_ARC_ARRAY      = Arrays.asList(XFT.PREFIX, "arc");
	private static final List<String> WORKFLOW_INV_ARRAY = Arrays.asList("wrk:workflowData", XdatStoredSearch.SCHEMA_ELEMENT_NAME, "xnat:fieldDefinitionGroup", "xnat:investigatorData");
	private static final List<String> WORKFLOW_ARRAY     = Arrays.asList("wrk:workflowData", XdatStoredSearch.SCHEMA_ELEMENT_NAME, "xnat:fieldDefinitionGroup");

	private static final Map<String, List<String>> protectedNamespace = new HashMap<String, List<String>>() {{
		put(SecurityManager.READ, XFT_ARRAY);
		put(SecurityManager.EDIT, XFT_ARC_ARRAY);
		put(SecurityManager.DELETE, XFT_ARC_ARRAY);
	}};
	private static final Map<String, List<String>> unsecured          = new HashMap<String, List<String>>() {{
		put(SecurityManager.READ, WORKFLOW_INV_ARRAY);
		put(SecurityManager.EDIT, WORKFLOW_INV_ARRAY);
		put(SecurityManager.DELETE, WORKFLOW_ARRAY);
	}};

	/* (non-Javadoc)
	 * User is authorized if:
	 *  the datatype is specifically marked as unsecured
	 *  the user is an administrator
	 *  or the data type is otherwise secured.
	 * @see org.nrg.xdat.security.AuthorizerI#authorizeSave(org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement, org.nrg.xft.security.UserI)
	 * 
	 */
	public void authorizeSave(final XFTItem e, final UserI user) throws Exception{
		authorize(SecurityManager.EDIT,e,user);
	}
	
	static boolean has_users=false;

	private static synchronized boolean hasUsers() throws Exception {
		if (!has_users) {
			has_users = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) AS USER_COUNT FROM xdat_user;", "USER_COUNT", PoolDBUtils.getDefaultDBName(), null) > 0;
		}
		return has_users;
	}
	
	private boolean requiresSecurity(String action,final GenericWrapperElement e, final UserI user) throws Exception{
		return !unsecured.get(action).contains(e.getXSIType()) && (user != null && !Roles.isSiteAdmin(user) || user == null && hasUsers());
	}

	public void authorize(String action, final GenericWrapperElement e, final UserI user) throws Exception {
		if (requiresSecurity(action, e, user)) {
			final String xsiType = e.getXSIType();
			if (user.isGuest() && !action.equalsIgnoreCase(SecurityManager.READ)) {
				throwException(new InvalidPermissionException(user.getUsername(), action, xsiType, null));
			}

			final String username = user.getUsername();
			if (protectedNamespace.get(action).contains(e.getType().getForeignPrefix())) {
				AdminUtils.sendAdminEmail(user, "Unauthorized Admin Data Access Attempt", "Unauthorized access of '" + xsiType + "' by '" + username + "' prevented.");
				throwException(new InvalidPermissionException(username, action, xsiType, null, "Only site administrators can read core documents."));
			} else if (!ElementSecurity.IsSecureElement(xsiType)) {
				AdminUtils.sendAdminEmail(user, "Unauthorized Data Access Attempt", "Unauthorized access of '" + xsiType + "' by '" + username + "' prevented.");
				throwException(new InvalidPermissionException(username, action, xsiType, null));
			}
		}
	}

	public void authorize(final String action, final XFTItem item, final UserI user) throws Exception {
		if (requiresSecurity(action, item.getGenericSchemaElement(), user)) {
			authorize(action, item.getGenericSchemaElement(), user);
			final String xsiType = item.getXSIType();
			final String idValue = item.getIDValue();
			if (ElementSecurity.IsSecureElement(xsiType)) {
				if (!Permissions.can(user, item, action)) {
					final String username = user != null ? user.getUsername() : Users.DEFAULT_GUEST_USERNAME;
					AdminUtils.sendAdminEmail(user, "Unauthorized Data Retrieval Attempt", "Unauthorized access of item " + idValue + " of data type '" + xsiType + "' by '" + username + "' prevented.");
					throwException(new InvalidPermissionException(username, action, xsiType, idValue));
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
	 *  the datatype is specifically marked as unsecured
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
