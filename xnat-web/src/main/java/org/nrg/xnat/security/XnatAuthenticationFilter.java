/*
 * web: org.nrg.xnat.security.XnatAuthenticationFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.turbine.utils.ProjectAccessRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Map;

public class XnatAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    @Autowired
    public void setAuthenticationManager(final AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }

    @Autowired
    public void setXnatProviderManager(final XnatProviderManager providerManager) {
        _providerManager = providerManager;
    }

    @Autowired
    @Override
    public void setAuthenticationSuccessHandler(final AuthenticationSuccessHandler handler) {
        super.setAuthenticationSuccessHandler(handler);
    }

    @Autowired
    @Override
    public void setAuthenticationFailureHandler(final AuthenticationFailureHandler handler) {
        super.setAuthenticationFailureHandler(handler);
    }

    @Autowired
    @Override
    public void setSessionAuthenticationStrategy(final SessionAuthenticationStrategy strategy) {
        super.setSessionAuthenticationStrategy(strategy);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = StringUtils.isNotBlank(request.getParameter("username")) ? request.getParameter("username") : request.getParameter("j_username");
        String password = StringUtils.isNotBlank(request.getParameter("password")) ? request.getParameter("password") : request.getParameter("j_password");

        // If we didn't find a username
        if (StringUtils.isBlank(username)) {
            // See if there's an authorization header.
            final String header = request.getHeader("Authorization");
            if (!StringUtils.isBlank(header) && header.startsWith("Basic ")) {
                try {
                    final byte[] base64Token = header.substring(6).getBytes("UTF-8");
                    final String token       = new String(Base64.decode(base64Token), "UTF-8");
                    final int    position    = token.indexOf(":");

                    if (position != -1) {
                        username = token.substring(0, position);
                        password = token.substring(position + 1);
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

        final String                              providerName = request.getParameter("login_method");
        final UsernamePasswordAuthenticationToken authRequest;

        if (StringUtils.isEmpty(providerName) && !StringUtils.isEmpty(username)) {
            // Try to guess the auth method
            final String authMethod = _providerManager.retrieveAuthMethod(username);
            if (StringUtils.isEmpty(authMethod)) {
                throw new BadCredentialsException("Missing login_method parameter.");
            } else {
                authRequest = _providerManager.buildUPTokenForAuthMethod(authMethod, username, password);
            }
        } else {
            authRequest = _providerManager.buildUPTokenForProviderName(providerName, username, password);
        }

        setDetails(request, authRequest);

        try {
            AccessLogger.LogServiceAccess(username, AccessLogger.GetRequestIp(request), "Authentication", "SUCCESS");
            Authentication auth = getAuthenticationManager().authenticate(authRequest);

            //Fixed XNAT-4409 by adding a check for a par parameter on login. If a PAR is present and valid, then grant the user that just logged in the appropriate project permissions.
            if(StringUtils.isNotBlank(request.getParameter("par"))){
                String parId = request.getParameter("par");
                request.getSession().setAttribute("par", parId);
                ProjectAccessRequest par = ProjectAccessRequest.RequestPARByGUID(parId, null);
                if (par.getApproved() != null || par.getApprovalDate() != null) {
                    logger.debug("PAR not approved or already accepted: " + par.getGuid());
                } else {
                    XDATUser user = new XDATUser(username);
                    par.process(user, true, EventUtils.TYPE.WEB_FORM, "", "");
                }
            }

            return auth;
        } catch (AuthenticationException e) {
            logFailedAttempt(username, request);
            throw e;
        } catch (UserNotFoundException e) {
            _log.error("",e);
        } catch (UserInitException e) {
            _log.error("",e);
        } catch (Exception e) {
            _log.error("",e);
        }
        return null;
    }

    public static void logFailedAttempt(String username, HttpServletRequest req) {
        if (!StringUtils.isBlank(username)) {
            final Integer uid = retrieveUserId(username);
            if (uid != null) {
                try {
                    XFTItem item = XFTItem.NewItem("xdat:user_login", null);
                    item.setProperty("xdat:user_login.user_xdat_user_id", uid);
                    item.setProperty("xdat:user_login.ip_address", AccessLogger.GetRequestIp(req));
                    item.setProperty("xdat:user_login.login_date", Calendar.getInstance(java.util.TimeZone.getDefault()).getTime());
                    SaveItemHelper.authorizedSave(item, null, true, false, (EventMetaI) null);
                } catch (Exception exception) {
                    _log.error(exception);
                }
            }
            AccessLogger.LogServiceAccess(username, AccessLogger.GetRequestIp(req), "Authentication", "FAILED");
        }
    }

    public static Integer retrieveUserId(String username) {
        synchronized (checked) {
            if (username == null) {
                return null;
            }

            if (checked.containsKey(username)) {
                return checked.get(username);
            }

            final Integer i = Users.getUserid(username);
            checked.put(username, i);

            return i;
        }
    }

    private static final Log                  _log    = LogFactory.getLog(XnatAuthenticationFilter.class);
    private static final Map<String, Integer> checked = Maps.newHashMap();

    private XnatProviderManager _providerManager;
}
