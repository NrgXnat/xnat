/*
 * web: org.nrg.xnat.restlet.guard.XnatSecureGuard
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.guard;

import org.apache.turbine.util.TurbineException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.representations.RESTLoginRepresentation;
import org.nrg.xnat.restlet.util.BrowserDetector;
import org.nrg.xnat.restlet.util.BrowserDetectorI;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.restlet.Filter;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class XnatSecureGuard extends Filter {
	private static final Logger logger     = LoggerFactory.getLogger(XnatSecureGuard.class);
	private static final String HTTP_REALM = "XNAT Protected Area";

	/**
	 * Attempts to log the user in, first by checking the for an existing
	 * session (breaks traditional REST), then by trying HTTP basic
	 * authentication. Stores the authenticated UserI in the HttpSession.
	 */
	@Override
	protected int beforeHandle(Request request, Response response) {
		if (authenticate(request)) {
			return CONTINUE;
		} else {
			unauthorized(request, response);
			return STOP;
		}
	}

	protected Representation loginRepresentation(Request request) {
		try {
			return new RESTLoginRepresentation(MediaType.TEXT_HTML, request, null);
		} catch (TurbineException e) {
			logger.error("",e);
			return new StringRepresentation("An error has occurred. Unable to load login page.");
		}
	}

	protected HttpServletRequest getHttpServletRequest(Request request) {
		return getRequestUtil().getHttpServletRequest(request);
	}

	protected RequestUtil getRequestUtil() {
		return new RequestUtil();
	}

	protected ChallengeRequest createChallengeRequest() {
		return new ChallengeRequest(ChallengeScheme.HTTP_BASIC, HTTP_REALM);
	}

	protected BrowserDetectorI getBrowserDetector() {
		return new BrowserDetector();
	}

	protected UserI getUser(String login) throws Exception {
		return Users.getUser(login);
	}

    private AliasTokenService getAliasTokenService() {
        if (_aliasTokenService == null) {
            _aliasTokenService = XDAT.getContextService().getBean(AliasTokenService.class);
        }
        return _aliasTokenService;
    }

	private boolean authenticate(Request request) {
		// THIS BREAKS THE TRADITIONAL REST MODEL
		// But, if the user is already logged into the website and navigates
		// to a REST GET, they shouldn't have to re-login , TO
		final HttpServletRequest httpRequest = getHttpServletRequest(request);
		final UserI sessionUser = XDAT.getUserDetails();
		if (sessionUser != null) {
            //Check for a CsrfToken if necessary.
            try {
                //isCsrfTokenOk either returns true or throws an exception...
                SecureAction.isCsrfTokenOk(httpRequest, false);
            } catch (Exception e) {
                throw new RuntimeException(e);//LOL.
            }
            return true;
        } else {
			final ChallengeResponse challengeResponse = request.getChallengeResponse();
			if (challengeResponse != null) {
				UserI user = authenticateBasic(challengeResponse);
				if (user != null) {
					return true;
				}
			} else {
				return !XDAT.getSiteConfigPreferences().getRequireLogin();
			}
		}
		return false;
	}

	private UserI authenticateBasic(ChallengeResponse challengeResponse) {
			final String username = challengeResponse.getIdentifier();
			final String password = new String(challengeResponse.getSecret());

        UserI user;

        try {
			user = getUser(username);
			if(!Authenticator.Authenticate(user, new Authenticator.Credentials(username, password))){
				user=null;
			}
		} catch (Exception e) {
			user = null;
		}

        if (user == null && AliasToken.isAliasFormat(username)) {
            AliasToken token = getAliasTokenService().locateToken(username);
            try {
                user = Users.getUser(token.getXdatUserId());
            } catch (Exception exception) {
                user = null;
            }
        }

		return user;
	}

	private void unauthorized(Request request, Response response) {
		final HttpServletRequest httpRequest = getHttpServletRequest(request);
		// the session wasn't good, so let's just clear it out
		httpRequest.getSession().invalidate();

		response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);

		// HACK - browser sniff to detect script vs browser (human) access.
		// Browser access should always get the Login.vm page, while scripts
		// should use the standard challenge request/response mechanism. Will
		// break if script spoofs user-agent as major browser.
		// http://nrg.wustl.edu/fogbugz/default.php?424
		if (getBrowserDetector().isBrowser(httpRequest)) {
			response.setEntity(loginRepresentation(request));
		} else {
			// standard 401 with a www-authenticate
			response.setChallengeRequest(createChallengeRequest());
		}
	}

    private AliasTokenService _aliasTokenService;
}
