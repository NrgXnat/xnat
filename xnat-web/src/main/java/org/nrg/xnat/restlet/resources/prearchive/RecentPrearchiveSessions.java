/*
 * web: org.nrg.xnat.restlet.resources.prearchive.RecentPrearchiveSessions
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.prearchive;

import org.nrg.action.ClientException;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xft.XFTTable;
import org.nrg.xnat.helpers.prearchive.DatabaseSession;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.util.*;

public class RecentPrearchiveSessions extends SecureResource {
	public final static String RECENT = "recent";
	// default number of days
	public final static int DEFAULT_RECENT = 10;
	
	public final int numDays;
	
	private int determineNumberOfDays (String recentParameter) throws ClientException {
		if (recentParameter.equals("true")){
			return RecentPrearchiveSessions.DEFAULT_RECENT;
		}
		else {
			try {
				Integer i = Integer.parseInt(recentParameter);
				if (i <= 0) {
					throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, 
			                  				  "\"recent\" should not be 0 or less that 0", 
			                  				  new Exception ("\"recent\" should not be 0 or less that 0"));
				}
				else {
					return i;
				}
			}
			catch (NumberFormatException e) {
				throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, 
						                  "\"recent\" should be a number or true", 
						                  new Exception ("\"recent\" should be a number or true"));
			}	
		}	
	}
	
	public boolean allowPut() {
		return false;
	}
	
	public boolean allowPost() {
		return false;
	}
	
	public RecentPrearchiveSessions(Context context, 
									Request request,
									Response response) 
									throws ClientException {
		super(context, request, response);
		
		int _numDays = -1;
		if (!this.containsQueryVariable(RecentPrearchiveSessions.RECENT)) {
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Please set the \"recent\" parameter");
		}
		else {
			_numDays = this.determineNumberOfDays(this.getQueryVariable(RecentPrearchiveSessions.RECENT));
		}
		
		this.numDays = _numDays;
		
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
		getVariants().add(new Variant(MediaType.TEXT_HTML));
		getVariants().add(new Variant(MediaType.TEXT_XML));
	}
	
	private static double diffInDays(long start, long end) {
		double seconds = Math.floor((end - start) / 1000);
		double minutes = Math.floor(seconds / 60);
		double hours = Math.floor(minutes / 60);
		double days = Math.floor(hours / 24);
		return days;
	}
	
	public Representation represent(final Variant variant) {
		final MediaType mt = overrideVariant(variant);
		long now = Calendar.getInstance().getTimeInMillis();
		XFTTable t = null;
		try {
			// sort session, most recent on top.
			List<SessionData> ss = PrearcDatabase.getAllSessions();
			Collections.sort(ss, new Comparator<SessionData>(){
				public int compare(SessionData s1, SessionData s2) {
					return s1.getUploadDate().compareTo(s2.getUploadDate());
				}
			});
			
			List<SessionData> mostRecent = new ArrayList<SessionData>();
			for (SessionData s : ss){
				double days = diffInDays(s.getUploadDate().getTime(), now);
				if (days <= this.numDays) {
					mostRecent.add(s);
				}
				else {
					// outside the date range
				}
			}

			ArrayList<ArrayList<Object>> rows = new ArrayList<ArrayList<Object>>();
			for (SessionData s: mostRecent) {
				if (UserHelper.getUserHelperService(getUser()).hasEditAccessToSessionDataByTag(s.getProject())){
					ArrayList<Object> row= new ArrayList<>();
					for (DatabaseSession v : DatabaseSession.values()) {
						// replace internal url with the external one that doesn't have
						// local filesystem information.
						if (v == DatabaseSession.URL) {
							row.add(s.getExternalUrl());
						}
						else {
							row.add(v.readSession(s));
						}
					}
					rows.add(row);	
				}
				else {
					// user doesn't have access to this session
				}
			}
			t = PrearcUtils.convertArrayLtoTable(rows);
		}
		catch (Exception e) {
			this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
		}
		
		return this.representTable(t, mt, new Hashtable<String,Object>());
	}
}
