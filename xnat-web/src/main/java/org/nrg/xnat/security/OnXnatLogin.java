/*
 * web: org.nrg.xnat.security.OnXnatLogin
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.services.XnatAppInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

@Component
public class OnXnatLogin extends SavedRequestAwareAuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        if (logger.isDebugEnabled()) {
            logger.debug("Request is to process authentication");
        }

        try {
            final UserI user = XDAT.getUserDetails();

            if (user != null) {
                final XFTItem item = XFTItem.NewItem("xdat:user_login", user);
                setItemPropertyValue(item, "xdat:user_login.user_xdat_user_id", user.getID());
                setItemPropertyValue(item, "xdat:user_login.login_date", Calendar.getInstance(TimeZone.getDefault()).getTime());
                setItemPropertyValue(item, "xdat:user_login.ip_address", AccessLogger.GetRequestIp(request));
                setItemPropertyValue(item, "xdat:user_login.session_id", request.getSession().getId());

                SaveItemHelper.authorizedSave(item, null, true, false, EventUtils.DEFAULT_EVENT(user, null));

                request.getSession().setAttribute("userHelper", UserHelper.getUserHelperService(user));
            }
        } catch (XFTInitException e) {
            logger.error("An error occurred accessing XFT", e);
        } catch (ElementNotFoundException e) {
            logger.error("Could not find the requested element " + e.ELEMENT, e);
        } catch (InvalidValueException e) {
            logger.error("An invalid value was submitted when creating the user login object", e);
        } catch (FieldNotFoundException e) {
            logger.error("The field {} was not found when creating the user login object of type {}", e.FIELD, "xdat:user_login");
        } catch (Exception e) {
            logger.error("An unknown error was found", e);
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

    private void setItemPropertyValue(final XFTItem item, final String property, final Object value) throws XFTInitException, ElementNotFoundException, FieldNotFoundException, InvalidValueException {
        try {
            item.setProperty(property, value);
        } catch (InvalidValueException e) {
            throw new InvalidValueException("An invalid value was set for the " + item.getXSIType() + " type property " + property + ": " + value);
        }
    }

    private static final Logger logger          = LoggerFactory.getLogger(XnatAppInfo.class);
    private static final String DEFAULT_LANDING = "/app/template/Index.vm?login=true";
}
