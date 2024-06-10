/*
 * web: org.nrg.xapi.authorization.SiteConfigPreferenceXapiAuthorization
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.authorization;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.xdat.preferences.SiteConfigAccess;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Checks whether user can access the system user list.
 */
@Component
@Slf4j
public class SiteConfigPreferenceXapiAuthorization extends AbstractXapiAuthorization {
    @Autowired
    public SiteConfigPreferenceXapiAuthorization(final SiteConfigAccess access) {
        _access = access;
    }

    @Override
    protected boolean checkImpl(final AccessLevel accessLevel, final JoinPoint joinPoint, final UserI user, final HttpServletRequest request) {
        final Object[] parameters = joinPoint.getArgs();

        // We only allow one parameter.
        if (ArrayUtils.isEmpty(parameters) || parameters.length > 1) {
            return false;
        }

        return parameters[0] instanceof List
               ? GenericUtils.convertToTypedList((List<?>) parameters[0], String.class).stream().allMatch(preference -> _access.canRead(user, preference))
               : _access.canRead(user, parameters[0] instanceof String ? (String) parameters[0] : parameters[0].toString());
    }

    @Override
    protected boolean considerGuests() {
        return true;
    }

    private final SiteConfigAccess _access;
}
