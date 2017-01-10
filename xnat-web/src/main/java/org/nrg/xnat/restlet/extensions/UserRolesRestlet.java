/*
 * web: org.nrg.xnat.restlet.extensions.UserRolesRestlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.extensions;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.services.RoleRepositoryServiceI.RoleDefinitionI;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.XnatRestlet;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * @author tim@deck5consulting.com
 *
 * Implementation of the User Roles functionality.  The post method adds or removes role for the specified user.
 */
@XnatRestlet("/user/{USER_ID}/roles")
public class UserRolesRestlet extends SecureResource {
    private static final Logger logger = LoggerFactory.getLogger(UserRolesRestlet.class);
	UserI other=null;
	String userId=null;
	/**
	 * @param context standard
	 * @param request standard
	 * @param response standard
	 */
	public UserRolesRestlet(Context context, Request request, Response response) {
		super(context, request, response);
		
		if (!Roles.isSiteAdmin(getUser())) {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "User does not have privileges to access this project.");
        } else {
            userId = (String) getRequest().getAttributes().get("USER_ID");
            if (StringUtils.isBlank(userId)) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "As of this release, you must specify a user on which to perform.");
                return;
            }else{
            	try {
					other=Users.getUser(userId);
				} catch (Exception e) {
					logger.error("",e);
				}
            }
            this.getVariants().add(new Variant(MediaType.ALL));
        }
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public void handlePost() {
		final List<String> roles=Lists.newArrayList();
		
		if(hasQueryVariable("roles")){
			Collections.addAll(roles, getQueryVariable("roles").split(","));
		}
		
		try {
			//relies on a configuration parameter which defines the available roles.
			Collection<RoleDefinitionI> defined=Roles.getRoles();
			
			final List<String> allDefinedRoles=Lists.newArrayList();
			for(RoleDefinitionI def:defined){
				allDefinedRoles.add(def.getKey());
			}

			final UserI user = getUser();
	        //remove roles and save one at a time so that there is a separate workflow entry for each one
			for(String dRole:allDefinedRoles){
				if(!roles.contains(dRole)){
					//remove role if it is there
					if(Roles.checkRole(other,dRole)){
						Roles.deleteRole(user,other, dRole);
						
						other=Users.getUser(userId);//get fresh db copy
					}
				}
			}
			
	        //add roles and save one at a time so that there is a separate workflow entry for each one
			for(String dRole:allDefinedRoles){
				if(roles.contains(dRole)){
					//add role if isn't there
					if(!Roles.checkRole(other,dRole)){
						Roles.addRole(user, other, dRole);

						other=Users.getUser(userId);//get fresh db copy
					}
				}
			}
				
			this.getResponse().setEntity(new StringRepresentation(""));
			this.getResponse().setStatus(Status.SUCCESS_ACCEPTED);
		} catch (Throwable e) {
			logger.error("",e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
		}
	}
	


	@Override
	public Representation represent(Variant variant) throws ResourceException {	
		MediaType mt = overrideVariant(variant);
		Hashtable<String,Object> params= new Hashtable<>();
		
		XFTTable table=new XFTTable();
		table.initTable(new String[]{"role"});
		for(String role: Roles.getRoles(getUser())){
			table.rows().add(new Object[]{role});
		}

		params.put("totalRecords", table.size());
		return this.representTable(table, mt, params);
	}
}
