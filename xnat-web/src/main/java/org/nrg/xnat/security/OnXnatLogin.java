/*
 * web: org.nrg.xnat.security.OnXnatLogin
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class OnXnatLogin extends SavedRequestAwareAuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) throws IOException, ServletException {
        log.debug("Request is to process authentication");

        try {
            Users.recordUserLogin(request);
        } catch (XFTInitException e) {
            log.error("An error occurred accessing XFT", e);
        } catch (ElementNotFoundException e) {
            log.error("Could not find the requested element " + e.ELEMENT, e);
        } catch (InvalidValueException e) {
            log.error("An invalid value was submitted when creating the user login object", e);
        } catch (FieldNotFoundException e) {
            log.error("The field {} was not found when creating the user login object of type {}", e.FIELD, "xdat:user_login");
        } catch (Exception e) {
            log.error("An unknown error was found", e);
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        // TODO: Isn't there an override for the default landing page?
        final String  url           = getDefaultTargetUrl();
        final boolean isRootDefault = "/".equals(url);

        if (isRootDefault) {
            setDefaultTargetUrl(DEFAULT_LANDING);
        }

        // If the app root is the default, go to the default landing, otherwise figure it out.
        return isRootDefault ? DEFAULT_LANDING : super.determineTargetUrl(request, response);
    }

    private static final String DEFAULT_LANDING = "/app/template/Index.vm?login=true";
}
