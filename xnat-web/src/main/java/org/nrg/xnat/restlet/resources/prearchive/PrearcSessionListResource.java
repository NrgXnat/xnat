/*
 * web: org.nrg.xnat.restlet.resources.prearchive.PrearcSessionListResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.prearchive;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.*;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

public final class PrearcSessionListResource extends SecureResource {

	private static final String PROJECT_ATTR = "PROJECT_ID";
	private final Logger logger = LoggerFactory.getLogger(PrearcSessionListResource.class);

	private final String requestedProject;
	private final Reference prearcRef;

	/**
	 * @param context
	 * @param request
	 * @param response
	 */
	public PrearcSessionListResource(final Context context, final Request request, final Response response) {
		super(context, request, response);

		// Project is explicit in the request
		requestedProject = (String)getParameter(request,PROJECT_ATTR);

		prearcRef = request.getResourceRef();
		
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
		getVariants().add(new Variant(MediaType.TEXT_HTML));
		getVariants().add(new Variant(MediaType.TEXT_XML));
		
        if (request.getMethod() == Method.PUT && !Roles.isSiteAdmin(getUser())) {
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Only administrators can request a rebuild of the prearchive.");
        }
	}
	
	/**
	 * Refresh all the sessions
	 */
	@Override
	public boolean allowPut () {
		return true;
	}
	
	public void handlePut () {
		try {
			PrearcDatabase.refresh(true);
		} catch (Exception e) {
			logger.error("Unable to refresh sessions", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
		}
	}
	
	/**
		 * (non-Javadoc)
	 * 
	 * @see
	 * org.restlet.resource.Resource#represent(org.restlet.resource.Variant)
	 */
	@Override
	public Representation represent(final Variant variant) throws ResourceException {
		final MediaType mt = overrideVariant(variant);

		XFTTable table;

		final UserI user = getUser();
		if(this.getQueryVariable("tag") != null){
			final String tag=getQueryVariable("tag");
			try {
				if(!Roles.isSiteAdmin(user)){
					this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Non admin user's can not query by tag");
					return null;
				}
				
				table=retrieveTable(tag);
			} catch (Exception e) {
				logger.error("", e);
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
				return null;
			}
		}else{		
			ArrayList<String> projects = null;
			
			try {
				projects = PrearcUtils.getProjects(user, requestedProject);
			} catch (Exception e) {
				logger.error(" Unable to get list of projects", e);
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
				return null;
			}
			
			String path=prearcRef.getBaseRef().toString();
		
			if(requestedProject!=null){
				path = StringUtils.join(new String[]{path,"/",requestedProject});
			}
						
			try {
				table = this.retrieveTable(projects);
			}
			catch (SQLException e) {
				logger.error("Unable to query prearchive table : ", e);
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
				return null;
			} catch (SessionException e) {
				logger.error("Unable to query prearchive table : ", e);
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
				return null;
			} catch (Exception e) {
				logger.error("Unable to query prearchive table : ", e);
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
				return null;
			}	
		}

			
		return this.representTable(table, mt, new Hashtable<String,Object>());
	}
	
	public XFTTable retrieveTable(ArrayList<String> projects) throws Exception, SQLException, SessionException {
		String [] _proj = new String[projects.size()];
		final XFTTable table=PrearcUtils.convertArrayLtoTable(PrearcDatabase.buildRows(projects.toArray(_proj)));
		return table;
	}
	
	public XFTTable retrieveTable(String tag) throws Exception, SQLException, SessionException {
		final Collection<SessionData> matches=PrearcDatabase.getSessionByUID(tag);

		final List<SessionDataTriple> ss=new ArrayList<SessionDataTriple>();
		
		for(final SessionData s:matches){
			ss.add(s.getSessionDataTriple());
		}
		
		final XFTTable table=PrearcUtils.convertArrayLtoTable(PrearcDatabase.buildRows(ss));
		
		return table;
	}
	
	public PrearcTableBuilderI getPrearcBuider(){
		return new PrearcTableBuilder();
	}
}
