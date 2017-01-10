/*
 * web: org.nrg.xnat.security.XnatLogoutHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class XnatLogoutHandler extends SecurityContextLogoutHandler implements LogoutHandler {
    @Autowired
    public XnatLogoutHandler(final SessionRegistry registry) {
        super();
        _registry = registry;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        super.logout(request, response, authentication);

        //expire that guy here.
        if(_registry!=null) {
            SessionInformation si = _registry.getSessionInformation(request.getSession().getId());
            if (si != null) {
                si.expireNow();
            }
        }

    }

    private final SessionRegistry _registry;
}

