/*
 * core: org.nrg.xapi.authorization.AdminXapiAuthorization
 * XNAT http://www.xnat.org
 * Copyright (c) 2017-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.authorization;

import org.aspectj.lang.JoinPoint;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.security.UserI;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Checks whether the user has one of the standard administrative roles, system admin, all data admin, or all data access.
 */
@Component
public class AdminXapiAuthorization extends AbstractXapiAuthorization {
    @Override
    protected boolean checkImpl(final AccessLevel accessLevel, final JoinPoint joinPoint, final UserI user, final HttpServletRequest request) {
        switch (accessLevel) {
            case DataAdmin:
                // Test whether the user is an administrator or all data admin.
                return Groups.hasAllDataAdmin(user);

            case DataAccess:
                // Test whether the user is an administrator, all data admin, or all data access.
                return Groups.hasAllDataAccess(user);

            default:
                // Test whether the user is an administrator.
                return Roles.isSiteAdmin(user);
        }
    }

    @Override
    protected boolean considerGuests() {
        return false;
    }
}
