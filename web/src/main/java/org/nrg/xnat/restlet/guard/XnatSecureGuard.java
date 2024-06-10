/*
 * web: org.nrg.xnat.restlet.guard.XnatSecureGuard
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.guard;

import lombok.extern.slf4j.Slf4j;
import org.apache.turbine.util.TurbineException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.representations.RESTLoginRepresentation;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.nrg.xnat.utils.InteractiveAgentDetector;
import org.restlet.Filter;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

@Slf4j
public class XnatSecureGuard extends Filter {
    /**
     * Attempts to log the user in, first by checking the for an existing
     * session (breaks traditional REST), then by trying HTTP basic
     * authentication. Stores the authenticated UserI in the HttpSession.
     */
    @Override
    protected int beforeHandle(Request request, Response response) {
        if (authenticate(request)) {
            return CONTINUE;
        }
        unauthorized(request, response);
        return STOP;
    }

    protected Representation loginRepresentation(Request request) {
        try {
            return new RESTLoginRepresentation(MediaType.TEXT_HTML, request, null);
        } catch (TurbineException e) {
            log.error("", e);
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

    protected UserI getUser(String login) throws Exception {
        return Users.getUser(login);
    }

    private Optional<UserI> getUser(final String username, final String password) {
        try {
            final UserI user = getUser(username);
            if (Authenticator.Authenticate(user, new Authenticator.Credentials(username, password))) {
                return Optional.of(user);
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    private AliasTokenService getAliasTokenService() {
        if (_aliasTokenService == null) {
            _aliasTokenService = XDAT.getContextService().getBean(AliasTokenService.class);
        }
        return _aliasTokenService;
    }

    private boolean authenticate(final Request request) {
        // THIS BREAKS THE TRADITIONAL REST MODEL
        // But, if the user is already logged into the website and navigates
        // to a REST GET, they shouldn't have to re-login , TO
        final HttpServletRequest httpRequest = getHttpServletRequest(request);
        final UserI              sessionUser = XDAT.getUserDetails();
        if (sessionUser != null) {
            //Check for a CsrfToken if necessary.
            try {
                //isCsrfTokenOk either returns true or throws an exception...
                return SecureAction.isCsrfTokenOk(httpRequest, false);
            } catch (Exception e) {
                throw new RuntimeException(e);//LOL.
            }
        }
        final ChallengeResponse challengeResponse = request.getChallengeResponse();
        return challengeResponse != null ? authenticateBasic(challengeResponse) != null : !XDAT.getSiteConfigPreferences().getRequireLogin();
    }

    private UserI authenticateBasic(final ChallengeResponse challengeResponse) {
        final String username = challengeResponse.getIdentifier();
        final String password = new String(challengeResponse.getSecret());
        return getUser(username, password).orElseGet(() -> getUserFromToken(username));
    }

    private UserI getUserFromToken(final String username) {
        if (AliasToken.isAliasFormat(username)) {
            try {
                final AliasToken token = getAliasTokenService().locateToken(username);
                if (token != null) {
                    return Users.getUser(token.getXdatUserId());
                }
            } catch (Exception e) {
                log.info("An error occurred trying to perform basic authentication for user {}", username, e);
            }
        }
        return null;
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
        if (getBrowserDetector().isInteractiveAgent(httpRequest)) {
            response.setEntity(loginRepresentation(request));
        } else {
            // standard 401 with a www-authenticate
            response.setChallengeRequest(createChallengeRequest());
        }
    }

    @Nonnull
    private InteractiveAgentDetector getBrowserDetector() {
        if (_detector == null) {
            _detector = XDAT.getContextService().getBean(InteractiveAgentDetector.class);
        }
        return _detector;
    }

    private static final String HTTP_REALM = "XNAT Protected Area";

    private InteractiveAgentDetector _detector;
    private AliasTokenService        _aliasTokenService;
}
