package org.nrg.xapi.authorization;

import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Roles;
import org.springframework.stereotype.Component;

/**
 * Checks whether the user is a data administrator.
 */
@Component
public class DataAdminXapiAuthorization extends AbstractXapiAuthorization {
    @Override
    protected boolean checkImpl() {
        return Roles.isSiteAdmin(getUser()) || Groups.isDataAdmin(getUser());
    }

    @Override
    protected boolean considerGuests() {
        return false;
    }
}
