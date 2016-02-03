/*
 * org.nrg.xnat.restlet.actions.PrearcBlankSession
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.restlet.data.Status;

public class PrearcBlankSession extends ImporterHandlerA {
	static Logger logger = Logger.getLogger(PrearcBlankSession.class);
	
	private static final String PROJECT = "project";
	private static final String SUID= "suid";
	private static final String SESSION = "session";
	private static final String SUBJECT = "subject"; // optional, added to the prearchive table if it is present
	
	private final UserI user;
	private final FileWriterWrapperI fi; // never used, we only check if it is null
	private final Map<String,Object> params;
	
	/**
	 * Check that \"project\", \"session\" and \"suid\" are present.  
	 * 
	 * @param params
	 * @param fi
	 * @throws ClientException
	 */
	private void validate(Map<String,Object> params, FileWriterWrapperI fi) throws ClientException {
		if (!params.containsKey(PROJECT) ||
			!params.containsKey(SUID) || 
			!params.containsKey(SESSION)) {
			throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,"Cannot build a blank row for this session without the \"project\", \"session\" and \"suid\" parameters", new IllegalArgumentException());
		}
		if (fi != null) {
			throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "Cannot upload binary data while creating a blank row for this session", new IllegalArgumentException());
		}
	}
	
	/**
	 * Helper class to create a blank row in the prearchive table  
	 * @param uID2
	 * @param u
	 * @param fi
	 * @param project_id
	 * @param additionalValues
	 */
	public PrearcBlankSession(final Object uID2 ,  // ignored
							   final UserI u, // ignored
							   final FileWriterWrapperI fi,  // should be null, we are not expecting a file when creating a blank row
							   Map<String,Object> params
							   ){
    	super((uID2==null)?u:uID2,u,fi,params);
    	this.user=u;
    	this.fi = fi;
    	this.params = params;
	}
	
	@Override
	public List<String> call() throws ClientException,ServerException{
		this.validate(this.params, this.fi);
		String project = (String) this.params.get(PROJECT);
		String session = (String) this.params.get(SESSION);
		String suid = (String) this.params.get(SUID);
		
		try {
			SessionData blankSession = PrearcUtils.blankSession(project, session, suid);
			Collection<SessionData> dupes = PrearcDatabase.getSessionByUID(suid);
			if (dupes.size() != 0) {
				throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, "A session with Study Instance UID " + suid + " exists.", new IllegalArgumentException());
			}
			blankSession.setStatus(PrearcStatus.RECEIVING);
			if (this.params.containsKey(SUBJECT)) {
				blankSession.setSubject((String)this.params.get(SUBJECT)); 
			}
			PrearcDatabase.addSession(blankSession);
			Map<String,Object> additionalValues = new HashMap<String,Object>();
			additionalValues.put(SUID, blankSession.getTag());
			List<String> ret = new ArrayList<String>();
			ret.add(PrearcUtils.buildURI(blankSession.getProject(), blankSession.getTimestamp(), blankSession.getFolderName()));
			return ret;
		}
		catch (Exception e) {
			logger.error("Unable to create blank session", e);
			throw new ClientException(Status.SERVER_ERROR_INTERNAL,e.getMessage(), new IllegalArgumentException());
		}
	}
}
