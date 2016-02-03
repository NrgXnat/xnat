/*
 * org.nrg.xnat.restlet.services.prearchive.PrearchiveBatchRebuild
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
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


public class PrearchiveBatchRebuild extends BatchPrearchiveActionsA {

	public PrearchiveBatchRebuild(Context context, Request request, Response response) {
		super(context, request, response);
				
	}

	@Override
	public void handlePost() {
		try {
			loadBodyVariables();

			//maintain parameters
			loadQueryVariables();
		} catch (ClientException e) {
			getResponse().setStatus(e.getStatus(),e);
			return;
		}

        final List<SessionDataTriple> ss= new ArrayList<>();
		
		for(final String src:srcs){
            File sessionDir;
            try {
                SessionDataTriple s=buildSessionDataTriple(src);
                ss.add(s);
                sessionDir = PrearcUtils.getPrearcSessionDir(user, s.getProject(), s.getTimestamp(), s.getFolderName(), false);

                final boolean overrideLock = hasQueryVariable("overrideLock") && Boolean.parseBoolean(getQueryVariable("overrideLock"));

                if (PrearcDatabase.setStatus(s.getFolderName(), s.getTimestamp(), s.getProject(), PrearcUtils.PrearcStatus.QUEUED_BUILDING, overrideLock)) {
                    SessionData sessionData = PrearcDatabase.getSession(s.getFolderName(), s.getTimestamp(), s.getProject());
                    PrearchiveOperationRequest request = new PrearchiveOperationRequest(user, sessionData, sessionDir, "Rebuild");
                    XDAT.sendJmsRequest(request);
                }
            } catch (Exception exception) {
                logger.error("Error when setting prearchive session status to QUEUED", exception);
                this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,exception);
            }
        }

		
		final Response response = getResponse();
		try {
			response.setEntity(updatedStatusRepresentation(ss,overrideVariant(getPreferredVariant())));
		} catch (Exception e) {
			logger.error("",e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e);
		}
	}

    private static final Logger logger = Logger.getLogger(PrearchiveBatchRebuild.class);
}
