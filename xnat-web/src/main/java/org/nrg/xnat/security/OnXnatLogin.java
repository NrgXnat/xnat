/*
 * web: org.nrg.xnat.security.OnXnatLogin
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class OnXnatLogin extends SavedRequestAwareAuthenticationSuccessHandler {

	protected final Log logger = LogFactory.getLog(getClass());
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {

        if (logger.isDebugEnabled()) {
            logger.debug("Request is to process authentication");
        }
        
        try{
			final UserI user = XDAT.getUserDetails();

            if (user != null) {
                Date today = Calendar.getInstance(java.util.TimeZone.getDefault()).getTime();
                XFTItem item = XFTItem.NewItem("xdat:user_login", user);
                item.setProperty("xdat:user_login.user_xdat_user_id", user.getID());
                item.setProperty("xdat:user_login.login_date", today);
                item.setProperty("xdat:user_login.ip_address", AccessLogger.GetRequestIp(request));
                item.setProperty("xdat:user_login.session_id", request.getSession().getId());
                SaveItemHelper.authorizedSave(item, null, true, false, EventUtils.DEFAULT_EVENT(user, null));

                request.getSession().setAttribute("userHelper", UserHelper.getUserHelperService(user));
            }
        } catch (Exception e) {
            logger.error(e);
        }
        super.onAuthenticationSuccess(request, response, authentication);
	}
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response){
        String loginLanding = "/app/template/Index.vm?login=true";
        String url = getDefaultTargetUrl();
        if("/".equals(url)){
            setDefaultTargetUrl(loginLanding);
            return loginLanding;
        } else {
            return super.determineTargetUrl(request, response);
        }
    }
}
