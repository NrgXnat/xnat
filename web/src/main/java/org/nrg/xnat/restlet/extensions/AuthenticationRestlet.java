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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.restlet.XnatRestlet;
import org.nrg.xnat.security.XnatProviderManager;
import org.nrg.xnat.security.provider.XnatAuthenticationProvider;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

@XnatRestlet(value = "/services/auth", secure = false)
@Slf4j
public class AuthenticationRestlet extends Resource {
    public AuthenticationRestlet(Context context, Request request, Response response) throws Exception {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.ALL));
        if (request.getMethod().equals(Method.GET)) {
            throw new Exception("You must POST or PUT authentication credentials in the request body.");
        }
        if (!request.isEntityAvailable()) {
            throw new Exception("You must provide authentication credentials in the request body.");
        }
        extractCredentials(request.getEntity().getText());
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
        log.debug("Passing a representation of the verify extensions restlet.");

        if (XDAT.getSiteConfigPreferences().getRequireLogin() && StringUtils.isAnyBlank(username, password)) {
            fail();
            return;
        }

        final XnatProviderManager manager = XDAT.getContextService().getBean(XnatProviderManager.class);

        Authentication authentication = null;

        try {
            if (!StringUtils.isEmpty(authenticatorId)) {
                Map<String, XnatAuthenticationProvider> visibleProviders = manager.getVisibleEnabledProviders();
                if (!visibleProviders.containsKey(authenticatorId)) {
                    visibleProviders = manager.getLinkedEnabledProviders(); //openId is a linked provider
                    if (!visibleProviders.containsKey(authenticatorId)) {
                        fail(Status.CLIENT_ERROR_BAD_REQUEST, String.format("No authentication provider identified by id %s found.", authenticatorId));
                        return;
                    }
                }
                authentication = manager.authenticate(manager.buildUPTokenForProviderName(authenticatorId, username, password));
            } else {
                if (!StringUtils.isEmpty(authMethod)) {
                    //Are there multiple authentication providers with the same authMethod
                    int countOfProvidersByAuthMethod = manager.countAuthenticatorsWithAuthMethod(authMethod);
                    if (countOfProvidersByAuthMethod > 1) {
                        fail(Status.CLIENT_ERROR_BAD_REQUEST, "Multiple authentication providers with identical authMethod exist. Use query parameter authenticatorId to specify a particular provider.");
                        return;
                    } else if (countOfProvidersByAuthMethod == 0) {
                        fail();
                        return;
                    }
                } else if (!StringUtils.isEmpty(username)) {
                    //try to guess the auth method
                    authMethod = manager.retrieveAuthMethod(username);
                    if (StringUtils.isEmpty(authMethod)) {
                        fail();
                    }
                }
                authentication = manager.authenticate(manager.buildUPTokenForAuthMethod(authMethod, username, password));
            }
            if (null != authentication && authentication.isAuthenticated()) {
                succeed(authentication);
                getResponse().setEntity(ServletCall.getRequest(getRequest()).getSession().getId(), MediaType.TEXT_PLAIN);
            } else {
                fail();
            }
        } catch (AuthenticationException e) {
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

    private void fail(final Status status, final String message) {
        log.debug("Authentication failed: {}", message);
        getResponse().setStatus(status, "Authentication failed: " + message);
    }

    private void extractCredentials(String text) {
        for (String entry : text.split("&")) {
            final String[] atoms = entry.split("=", 2);
            if (atoms.length < 2) {
                // TODO: Just ignoring for now, should we do something here?
                log.warn("Found insufficient number of atoms in credential entry: " + entry);
            } else {
                try {
                    switch (atoms[0]) {
                        case "username":
                        case "j_username":
                            username = URLDecoder.decode(atoms[1], "UTF-8");
                            break;
                        case "password":
                        case "j_password":
                            password = URLDecoder.decode(atoms[1], "UTF-8");
                            break;
                        case "provider":
                        case "login_method":
                            authMethod = URLDecoder.decode(atoms[1], "UTF-8");
                            break;
                        case "authenticatorId":
                            authenticatorId = URLDecoder.decode(atoms[1], "UTF-8");
                        default:
                            // TODO: Just ignoring for now, should we do something here?
                            log.warn("Unknown credential property: " + atoms[0]);
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

    private String authMethod;
    private String authenticatorId;
    private String username;
    private String password;
}
