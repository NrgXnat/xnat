package org.nrg.xapi.authorization;

import org.aspectj.lang.JoinPoint;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.security.UserI;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Checks whether the user is a data administrator.
 */
@Component
public class AllDataAdminXapiAuthorization extends AbstractXapiAuthorization {
    @Override
    protected boolean checkImpl(final AccessLevel accessLevel, final JoinPoint joinPoint, final UserI user, final HttpServletRequest request) {
        return Roles.isSiteAdmin(user) || Groups.isDataAdmin(user);
    }

    @Override
    protected boolean considerGuests() {
        return false;
    }
}
