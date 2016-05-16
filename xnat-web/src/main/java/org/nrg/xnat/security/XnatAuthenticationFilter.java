/*
 * org.nrg.xnat.security.XnatAuthenticationFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.security;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.utils.SaveItemHelper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Map;

public class XnatAuthenticationFilter extends UsernamePasswordAuthenticationFilter{

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = StringUtils.isNotBlank(request.getParameter("username")) ? request.getParameter("username") : request.getParameter("j_username");
        String password = StringUtils.isNotBlank(request.getParameter("password")) ? request.getParameter("password") : request.getParameter("j_password");

        // If we didn't find a username
        if (StringUtils.isBlank(username)) {
            // See if there's an authorization header.
            String header = request.getHeader("Authorization");
            if (!StringUtils.isBlank(header) && header.startsWith("Basic ")) {
                byte[] base64Token;
                try {
                    base64Token = header.substring(6).getBytes("UTF-8");
                    String token = new String(Base64.decode(base64Token), "UTF-8");
                    int delim = token.indexOf(":");

                    if (delim != -1) {
                        username = token.substring(0, delim);
                        password = token.substring(delim + 1);
                    }
                    if (_log.isDebugEnabled()) {
                        _log.debug("Basic Authentication Authorization header found for user '" + username + "'");
                    }
                } catch (UnsupportedEncodingException exception) {
                    _log.error("Encoding exception on authentication attempt", exception);
                }
            }
        }

        //SHOULD we be throwing an exception if the username is null?

        String providerName=request.getParameter("login_method");
        UsernamePasswordAuthenticationToken authRequest;

        if(StringUtils.isEmpty(providerName) && !StringUtils.isEmpty(username)){
            //try to guess the auth_method
        	String auth_method = _providerManager.retrieveAuthMethod(username);
            if(StringUtils.isEmpty(auth_method)){
                throw new BadCredentialsException("Missing login_method parameter.");
            }
            else {
                authRequest=_providerManager.buildUPTokenForAuthMethod(auth_method,username,password);
            }
        }
        else {
            authRequest=_providerManager.buildUPTokenForProviderName(providerName,username,password);
        }

        setDetails(request, authRequest);

        try {
			Authentication auth= super.getAuthenticationManager().authenticate(authRequest);
			 AccessLogger.LogServiceAccess(username, AccessLogger.GetRequestIp(request), "Authentication", "SUCCESS");
			return auth;
		} catch (AuthenticationException e) {
			 logFailedAttempt(username, request);
			 throw e;
		}
    }

    public static void logFailedAttempt(String username, HttpServletRequest req){
    	if (!StringUtils.isBlank(username)) {
	    	 Integer uid=retrieveUserId(username);
			 if(uid!=null){
				try {
					XFTItem item = XFTItem.NewItem("xdat:user_login",null);
					item.setProperty("xdat:user_login.user_xdat_user_id",uid);
					item.setProperty("xdat:user_login.ip_address",AccessLogger.GetRequestIp(req));
                    item.setProperty("xdat:user_login.login_date", Calendar.getInstance(java.util.TimeZone.getDefault()).getTime());
					SaveItemHelper.authorizedSave(item,null,true,false,(EventMetaI)null);
                } catch (Exception exception) {
                    _log.error(exception);
				}
			 }
			 AccessLogger.LogServiceAccess(username, AccessLogger.GetRequestIp(req), "Authentication", "FAILED");
    	}
    }
    
    public static Integer retrieveUserId(String username){
    	synchronized(checked){
	    	if(username==null){
	    		return null;
	    	}
	    	
	    	if(checked.containsKey(username)){
	    		return checked.get(username);
	    	}
	    	
	    	Integer i=Users.getUserid(username);
	    	checked.put(username, i);
	    	
	    	return i;
    	}
    }
    
    private static final Log _log = LogFactory.getLog(XnatAuthenticationFilter.class);
    private static final Map<String, Integer> checked = Maps.newHashMap();

    @Inject
    private XnatProviderManager _providerManager;
}
