package org.nrg.xapi.authorization;

import org.aspectj.lang.JoinPoint;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xft.security.UserI;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Checks whether the user is a site administrator.
 */
@Component
public class AuthenticatedXapiAuthorization extends AbstractXapiAuthorization {
    @Override
    protected boolean considerGuests() {
        return false;
    }

    @Override
    protected boolean checkImpl(final AccessLevel accessLevel, final JoinPoint joinPoint, final UserI user, final HttpServletRequest request) {
        // This will actually never get hit because the considerGuests() bans guests in the first place.
        return true;
    }
}
