/*
 * org.nrg.xnat.restlet.actions.UserSessionId
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.actions;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.representations.JSONObjectRepresentation;
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
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

public class UserSessionId extends SecureResource {

	final private String userID;

	public UserSessionId(Context context, Request request, Response response) throws Exception {
		super(context, request, response);

        final UserI user = getUser();
		userID = (String) getParameter(request, "USER_ID");
        if (!Roles.isSiteAdmin(user) && !user.getLogin().equals(userID)) {
            _log.error("User " + user.getLogin() + " attempted to access session list for user " + userID);
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Only administrators can get the session list for users other than themselves.");
        } else {
            if (_log.isDebugEnabled()) {
                _log.debug(user.getLogin() + " is retrieving active sessions for user " + userID);
            }
            getVariants().add(new Variant(MediaType.ALL));
        }
	}

    @Override
    public Representation represent(Variant variant) {
        SessionRegistry sessionRegistry = XDAT.getContextService().getBean("sessionRegistry", SessionRegistryImpl.class);
		List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
		List<SessionInformation> l = null;
		for (Object p : allPrincipals) {
			if (p instanceof UserI) {
				if (((UserI) p).getLogin().equalsIgnoreCase(userID)) {
					l = sessionRegistry.getAllSessions(p, false);
				}
			}
		}
        try {
		if (l == null) {
			JSONObject json = new JSONObject();
			json.put(userID, 0);
                return new JSONObjectRepresentation(null, json);
		} else {
			JSONObject json = new JSONObject();
                json.put(userID, l.size());
                return new JSONObjectRepresentation(null, json);
            }
        } catch (JSONException e) {
            _log.error("There was an error processing the JSON", e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e, "There was an error processing the JSON");
            return null;
		}

	}

    private static final Logger _log = LoggerFactory.getLogger(UserSessionId.class);
}
