package org.nrg.xapi.authorization;

import org.nrg.xdat.security.helpers.Roles;
import org.springframework.stereotype.Component;

/**
 * Checks whether the user is a site administrator.
 */
@Component
public class UserXapiAuthorization extends AbstractXapiAuthorization {
    @Override
    protected boolean checkImpl() {
        // Admins can access all user-scoped calls.
        return Roles.isSiteAdmin(getUser()) || getUsernames(getJoinPoint()).contains(getUsername());
    }

    @Override
    protected boolean considerGuests() {
        return false;
    }
}
