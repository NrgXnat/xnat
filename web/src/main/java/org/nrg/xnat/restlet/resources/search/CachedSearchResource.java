/*
 * web: org.nrg.xnat.restlet.resources.search.CachedSearchResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.search;

import com.noelios.restlet.ext.servlet.ServletCall;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.db.MaterializedViewI;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.presentation.RESTHTMLPresenter;
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

public class CachedSearchResource extends SecureResource {
	private static final Logger logger = LoggerFactory.getLogger(CachedSearchResource.class);
	String tableName=null;
	
	Integer offset=null;
	Integer rowsPerPage=null;
	String sortBy=null;
	String sortOrder="ASC";

	public CachedSearchResource(Context context, Request request, Response response) {
		super(context, request, response);
		tableName = (String) getParameter(request, "CACHED_SEARCH_ID");

		final UserI user = getUser();

		if (this.getQueryVariable("offset") != null) {
			try {
				offset = Integer.valueOf(this.getQueryVariable("offset"));
			} catch (NumberFormatException e) {
				response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
				return;
			}
		}

		if (this.getQueryVariable("limit") != null) {
			try {
				rowsPerPage = Integer.valueOf(this.getQueryVariable("limit"));
			} catch (NumberFormatException e) {
				response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
				return;
			}
		}

		if (this.getQueryVariable("sortBy") != null) {
			sortBy = this.getQueryVariable("sortBy");
			if (PoolDBUtils.HackCheck(sortBy)) {
				AdminUtils.sendAdminEmail(user, "Possible SQL Injection Attempt", "SORT BY:" + sortOrder);
				response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
				return;
			}
			sortBy = StringUtils.replace(sortBy, " ", "");
		}

		if (this.getQueryVariable("sortOrder") != null) {
			sortOrder = this.getQueryVariable("sortOrder");
			if (PoolDBUtils.HackCheck(sortOrder)) {
				AdminUtils.sendAdminEmail(user, "Possible SQL Injection Attempt", "SORT ORDER:" + sortOrder);
				response.setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY);
				return;
			}
			sortOrder = StringUtils.replace(sortOrder, " ", "");
		}

		this.getVariants().add(new Variant(MediaType.APPLICATION_JSON));
		this.getVariants().add(new Variant(MediaType.TEXT_HTML));
		this.getVariants().add(new Variant(MediaType.TEXT_XML));
	}

	@Override
	public Representation getRepresentation(Variant variant) {	
		Hashtable<String,Object> params= new Hashtable<>();
		if(tableName!=null){
			params.put("ID", tableName);
		}
		XFTTable table=null;
		
		try {
			final UserI user = getUser();
			MaterializedViewI mv = MaterializedView.retrieveView(tableName, user);
			if(mv.getUser_name().equals(user.getLogin())){
				MediaType mt = this.getRequestedMediaType();
				final String sortOptions = (sortBy!=null) ? sortBy + " " + sortOrder : null;
				table=mv.getData(sortOptions, offset, rowsPerPage);
				if (null != sortOptions) {
					table.isSorted(true);
				}
				if (mt!=null && (mt.equals(SecureResource.APPLICATION_XLIST))){
					DisplaySearch ds = mv.getDisplaySearch(user);
			    	RESTHTMLPresenter presenter= new RESTHTMLPresenter(TurbineUtils.GetRelativePath(ServletCall.getRequest(this.getRequest())),null,user,sortBy);
			    	presenter.setRootElement(ds.getRootElement());
					presenter.setDisplay(ds.getDisplay());
					presenter.setAdditionalViews(ds.getAdditionalViews());
					table = (XFTTable)presenter.formatTable(table,ds,ds.allowDiffs);
			    }
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
