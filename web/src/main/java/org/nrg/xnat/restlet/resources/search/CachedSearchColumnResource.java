/*
 * web: org.nrg.xnat.restlet.resources.search.CachedSearchColumnResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.search;

import org.nrg.xft.XFTTable;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.db.MaterializedViewI;
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

public class CachedSearchColumnResource extends SecureResource {
	private static final Logger logger = LoggerFactory.getLogger(CachedSearchResource.class);
	String tableName=null;
	String columnName=null;
	
	public CachedSearchColumnResource(Context context, Request request, Response response) {
		super(context, request, response);
			
		tableName=(String)getParameter(request,"CACHED_SEARCH_ID");
		columnName=(String)getParameter(request,"COLUMN");
					
		this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
		this.getVariants().add(new Variant(MediaType.TEXT_HTML));
		this.getVariants().add(new Variant(MediaType.TEXT_XML));
	}



	@Override
	public Representation represent(Variant variant) {
		Hashtable<String,Object> params= new Hashtable<>();
		if(tableName!=null){
			params.put("ID", tableName);
		}
		if(columnName!=null){
			params.put("COLUMN", columnName);
		}
		XFTTable table=null;
		
		try {
			final UserI user = getUser();
			final MaterializedViewI mv = MaterializedView.retrieveView(tableName, user);
			if(mv.getUser_name().equals(user.getLogin())){
				table=mv.getColumnValues(columnName);
			}
		} catch (SQLException e) {
			logger.error("",e);
			this.getResponse().setStatus(Status.CLIENT_ERROR_GONE);
			table = new XFTTable();
		} catch (Exception e) {
			logger.error("",e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
			table = new XFTTable();
		}

		MediaType mt = overrideVariant(variant);
		
		return this.representTable(table, mt, params);
	}
}
