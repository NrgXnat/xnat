/*
 * org.nrg.xnat.restlet.guard.XnatSecureGuard
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 10/14/13 5:42 PM
 */
package org.nrg.xnat.restlet.guard;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.turbine.util.TurbineException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xft.XFT;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.representations.RESTLoginRepresentation;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.restlet.util.BrowserDetector;
import org.nrg.xnat.restlet.util.BrowserDetectorI;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.restlet.Filter;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

public class XnatSecureGuard extends Filter {
	static org.apache.log4j.Logger logger = Logger.getLogger(XnatSecureGuard.class);
	private static final String HTTP_REALM = "XNAT Protected Area";

	/**
	 * Attempts to log the user in, first by checking the for an existing
	 * session (breaks traditional REST), then by trying HTTP basic
	 * authentication. Stores the authenticated UserI in the HttpSession.
	 */
	@Override
	protected int beforeHandle(Request request, Response response) {
		if (authenticate(request, response)) {
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

	private boolean authenticate(Request request, Response response) {
		// THIS BREAKS THE TRADITIONAL REST MODEL
		// But, if the user is already logged into the website and navigates
		// to a REST GET, they shouldn't have to re-login , TO
		final HttpServletRequest httpRequest = getHttpServletRequest(request);
		final UserI sessionUser = getSessionUser(httpRequest);
		if (sessionUser != null) {
				//Check for a CsrfToken if necessary.
				try {
					//isCsrfTokenOk either returns true or throws an exception...
					SecureAction.isCsrfTokenOk(httpRequest,false);
				} catch (Exception e){
					throw new RuntimeException(e);//LOL.
				}
			
			attachUser(request, sessionUser);
			return true;
		} else {
			try {
                UserI user = null;
				final ChallengeResponse challengeResponse = request
						.getChallengeResponse();
				if (challengeResponse != null) {
					user = authenticateBasic(challengeResponse);
					if (user != null) {
						attachUser(request, user);
						httpRequest.getSession().setAttribute("XNAT_CSRF", UUID.randomUUID().toString());
						return true;
					}
				}
                else if (!XFT.GetRequireLogin()) {
                    try {
                        HttpSession session = httpRequest.getSession();
                        session.removeAttribute("loggedin");
                        user=Users.getGuest();
                        if (user!=null) {
                            attachUser(request, user);
                            return true;
                        }
                    } catch (Exception e) {
                        logger.error("",e);
                    }
                }
			} catch (RuntimeException e) {
				// We let this return an error to cause a 500 to return to the user.  The only other
				// option is to throw a 401.  But this wouldn't inform the user that there was an error.
				throw e;
			}
		}
		return false;
	}

	private UserI getSessionUser(HttpServletRequest httpRequest) {
		if(XDAT.getUserDetails()!=null){
			return XDAT.getUserDetails();
		}
		else{
			return (UserI) httpRequest.getSession().getAttribute(SecureResource.USER_ATTRIBUTE);
		}
	}

	private void attachUser(Request request, UserI user) {
		request.getAttributes().put(SecureResource.USER_ATTRIBUTE, user);
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
