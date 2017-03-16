/*
 * web: org.nrg.xnat.restlet.resources.search.SavedSearchListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.search;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

public class SavedSearchListResource extends SecureResource {
	private static final Logger logger = LoggerFactory.getLogger(SavedSearchListResource.class);

	public SavedSearchListResource(Context context, Request request, Response response) {
		super(context, request, response);
			this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
			this.getVariants().add(new Variant(MediaType.TEXT_HTML));
			this.getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	public List<String> retrieveAllTags(){
		final UserI user = getUser();
		try {
			return (List<String>)(XFTTable.Execute("SELECT tag from xdat_stored_search", user.getDBName(), user.getLogin()).convertColumnToArrayList("tag"));
		} catch (SQLException e) {
			logger.error("",e);
		} catch (DBPoolException e) {
			logger.error("",e);
		}
		
		return Lists.newArrayList();
	}

	@Override
	public Representation getRepresentation(Variant variant) {	
		MediaType mt = overrideVariant(variant);
		XFTTable table=null;
		String usernameToGetListFor = this.getQueryVariable("user");
		UserI userToGetListFor = null;
		try {
			UserI executingUser = getUser();
			if(Groups.isDataAdmin(executingUser) && !StringUtils.isBlank(usernameToGetListFor)) {
				userToGetListFor = new XDATUser(usernameToGetListFor);
			}
			else{
				userToGetListFor = executingUser;
			}
			String query="SELECT DISTINCT xss.* FROM xdat_stored_search xss LEFT JOIN xdat_stored_search_allowed_user xssau ON xss.id=xssau.xdat_stored_search_id LEFT JOIN xdat_stored_search_groupid xssag ON xss.id=xssag.allowed_groups_groupid_xdat_sto_id LEFT JOIN xdat_user_groupid ON xssag.groupid=xdat_user_groupid.groupid WHERE (xss.secure=0 OR xssau.login='" + userToGetListFor.getLogin() +"' OR groups_groupid_xdat_user_xdat_user_id="+ userToGetListFor.getID() + ")";
			String includeTagged = this.getQueryVariable("includeTag");
			if(includeTagged!=null){
				if(includeTagged.equals("true")){
					query +=" AND xss.tag IS NOT NULL";
				}else{
					if(retrieveAllTags().contains(includeTagged)){
						query +=" AND xss.tag='" + includeTagged +"'";
					}else{
						logger.error("",new Exception("Unknown tag: "+ includeTagged));
						this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
						return null;
					}
				}
			}else{
				query +=" AND xss.tag IS NULL";
			}
			table = XFTTable.Execute(query, executingUser.getDBName(), executingUser.getLogin());
		} catch (SQLException e) {
			logger.error("",e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			return null;
		} catch (DBPoolException e) {
			logger.error("",e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			return null;
		} catch (UserNotFoundException e) {
			logger.error("",e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			return null;
		} catch (UserInitException e) {
			logger.error("",e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			return null;
		}
		
		Hashtable<String,Object> params= new Hashtable<>();
		params.put("title", "Stored Searches");
		
		return this.representTable(table, mt, params);

	}
}
