/*
 * org.nrg.xnat.restlet.services.prearchive.BatchPrearchiveActionsA
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.services.prearchive;

import org.nrg.action.ClientException;
import org.nrg.xft.XFTTable;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionDataTriple;
import org.nrg.xnat.helpers.prearchive.SessionException;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

public abstract class BatchPrearchiveActionsA extends SecureResource {
	public static final String SRC = "src";
	public static final String ASYNC = "async";

	protected List<String> srcs = new ArrayList<String>();
	protected boolean async = true;

	public boolean allowGet() {
		return false;
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	public BatchPrearchiveActionsA(Context context, Request request, Response response) {
		super(context, request, response);
	}

	@Override
	public void handleParam(String key,Object value) throws ClientException {
		if(key.equals(SRC)){
			srcs.add((String)value);
		}
		else if (key.equals(ASYNC)) {
			boolean isFalse = isFalse(value);
			async = !isFalse;
		}	
	}				

	protected SessionDataTriple buildSessionDataTriple(String uri) throws MalformedURLException {
		return SessionDataTriple.fromURI(uri);
	}
	
	public Representation updatedStatusRepresentation(final Collection<SessionDataTriple> ss, final MediaType mt)	throws Exception, SQLException, SessionException {
		final XFTTable table=PrearcUtils.convertArrayLtoTable(PrearcDatabase.buildRows(ss));
		return this.representTable(table, mt, new Hashtable<String,Object>());
	}
}