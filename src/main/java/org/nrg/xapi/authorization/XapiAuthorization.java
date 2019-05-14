package org.nrg.xapi.authorization;

import org.aspectj.lang.JoinPoint;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotAuthenticatedException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xft.security.UserI;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides a means to extend how users can be tested for authorization when accessing an XAPI REST method.
 */
public interface XapiAuthorization {
    void check(final AccessLevel accessLevel, final JoinPoint joinPoint, final UserI user, final HttpServletRequest request) throws InsufficientPrivilegesException, NotAuthenticatedException, NotFoundException;
}
