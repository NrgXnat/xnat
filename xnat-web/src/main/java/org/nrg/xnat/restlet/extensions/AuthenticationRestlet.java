/*
 * web: org.nrg.xnat.restlet.extensions.AuthenticationRestlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.extensions;

import com.noelios.restlet.ext.servlet.ServletCall;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.restlet.XnatRestlet;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.nrg.xnat.security.XnatProviderManager;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@XnatRestlet(value = "/services/auth", secure = false)
public class AuthenticationRestlet extends Resource {
    private static final Log _log = LogFactory.getLog(AuthenticationRestlet.class);

    public AuthenticationRestlet(Context context, Request request, Response response) throws Exception {
        super(context, request, response);
        this.getVariants().add(new Variant(MediaType.ALL));
        if (request.getMethod().equals(Method.GET)) {
            throw new Exception("You must POST or PUT authentication credentials in the request body.");
        }
        if (request.isEntityAvailable()) {
            extractCredentials(request.getEntity().getText());
        } else {
            throw new Exception("You must provide authentication credentials in the request body.");
        }
    }

    @Override
    public boolean allowGet() {
        return false;
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void handlePut() {
        runAuthenticate();
    }

    @Override
    public void handlePost() {
        runAuthenticate();
    }

    private void runAuthenticate() {
        if (_log.isDebugEnabled()) {
            _log.debug("Passing a representation of the verify extensions restlet.");
        }

        if (XDAT.getSiteConfigPreferences().getRequireLogin() && (StringUtils.isBlank(_username) || StringUtils.isBlank(_password))) {
            fail();
            return;
        }

        final XnatProviderManager manager = XDAT.getContextService().getBean(XnatProviderManager.class);

        if(StringUtils.isEmpty(_authMethod) && !StringUtils.isEmpty(_username)){
            //try to guess the auth method
            _authMethod = manager.retrieveAuthMethod(_username);
            if(StringUtils.isEmpty(_authMethod)) {
                throw new BadCredentialsException("Missing login method parameter.");
            }
        }

        UsernamePasswordAuthenticationToken authRequest = manager.buildUPTokenForAuthMethod(_authMethod, _username, _password);
        Authentication authentication = manager.authenticate(authRequest);
        if (authentication.isAuthenticated()) {
            succeed(authentication);
            getResponse().setEntity(ServletCall.getRequest(getRequest()).getSession().getId(), MediaType.TEXT_PLAIN);
        } else {
            fail();
        }
    }

    private void succeed(final Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        getResponse().setStatus(Status.SUCCESS_OK, "OK");
    }

    private void fail() {
        getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Authentication failed.");
    }

    private void extractCredentials(String text) {
        String[] entries = text.split("&");
        for (String entry : entries) {
            String[] atoms = entry.split("=", 2);
            if (atoms.length < 2) {
                // TODO: Just ignoring for now, should we do something here?
                _log.warn("Found insufficient number of atoms in credential entry: " + entry);
            } else {
                try {
                    switch (atoms[0]) {
                        case "username":
                        case "j_username":
                            _username = URLDecoder.decode(atoms[1], "UTF-8");
                            break;
                        case "password":
                        case "j_password":
                            _password = URLDecoder.decode(atoms[1], "UTF-8");
                            break;
                        case "provider":
                        case "login_method":
                            _authMethod = URLDecoder.decode(atoms[1], "UTF-8");
                            break;
                        default:
                            // TODO: Just ignoring for now, should we do something here?
                            _log.warn("Unknown credential property: " + atoms[0]);
                            break;
                    }
                } catch (UnsupportedEncodingException e) {
                    // This is the dumbest exception in the history of humanity: the form of this method that doesn't
                    // specify an encoding is deprecated, so you have to specify an encoding. But the form of the method
                    // that takes an encoding (http://bit.ly/yX56fe) has an note that emphasizes that you should only
                    // use UTF-8 because "[n]ot doing so may introduce incompatibilities." Got it? You have to specify
                    // it, but it should always be the same thing. Oh, and BTW? You have to catch an exception for
                    // unsupported encodings because you may specify that one acceptable encoding or... something.
                    //
                    // I hate them.
                }
            }
        }
    }

    private String _authMethod;
    private String _username;
    private String _password;
}
