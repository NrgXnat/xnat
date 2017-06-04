package org.nrg.xapi.authorization;

import org.nrg.xdat.security.helpers.Roles;
import org.springframework.stereotype.Component;

/**
 * Checks whether the user is a site administrator.
 */
@Component
public class AdminXapiAuthorization extends AbstractXapiAuthorization {
    @Override
    protected boolean checkImpl() {
        // Test whether the user is an administrator.
        return Roles.isSiteAdmin(getUser());
    }

    @Override
    protected boolean considerGuests() {
        return false;
    }
}
