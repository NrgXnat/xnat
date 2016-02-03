/*
 * org.nrg.xnat.restlet.services.prearchive.PrearchiveBatchDelete
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 12/19/13 3:01 PM
 */

/**
 * 
 */
package org.nrg.xnat.restlet.services.prearchive;

import org.apache.log4j.Logger;
import org.nrg.action.ClientException;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.helpers.prearchive.SessionData;
import org.nrg.xnat.helpers.prearchive.SessionDataTriple;
import org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequest;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tolsen01
 *
 */
public class PrearchiveBatchDelete extends BatchPrearchiveActionsA {
    static org.apache.log4j.Logger logger = Logger.getLogger(PrearchiveBatchDelete.class);
    
	public PrearchiveBatchDelete(Context context, Request request, Response response) {
		super(context, request, response);
				
	}

	@Override
	public void handlePost() {
		try {
			loadBodyVariables();

			//maintain parameters
			loadQueryVariables();
		} catch (ClientException e) {
			this.getResponse().setStatus(e.getStatus(),e);
			return;
		}
		
		List<SessionDataTriple> ss=new ArrayList<SessionDataTriple>();
		
		for(final String src:srcs){
            File sessionDir;
			try {
				SessionDataTriple s=buildSessionDataTriple(src);
				if (!PrearcUtils.canModify(user, s.getProject())) {
					this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN,"Invalid permissions for new project.");
					return;
				}
				ss.add(s);
                sessionDir = PrearcUtils.getPrearcSessionDir(user, s.getProject(), s.getTimestamp(), s.getFolderName(), false);

                if (PrearcDatabase.setStatus(s.getFolderName(), s.getTimestamp(), s.getProject(), PrearcUtils.PrearcStatus.QUEUED_DELETING)) {
                    SessionData session = PrearcDatabase.getSession(s.getFolderName(), s.getTimestamp(), s.getProject());
                    XDAT.sendJmsRequest(new PrearchiveOperationRequest(user, session, sessionDir, "Delete"));
                }
			} catch (Exception e) {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e);
				return;
			}
		}
		
		final Response response = getResponse();
		try {
			response.setEntity(updatedStatusRepresentation(ss,overrideVariant(getPreferredVariant())));
		} catch (Exception e) {
			logger.error("",e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e);
			return;
		}
	}
}
