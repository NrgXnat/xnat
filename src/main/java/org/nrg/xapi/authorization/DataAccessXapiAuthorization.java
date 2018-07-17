package org.nrg.xapi.authorization;

import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Roles;
import org.springframework.stereotype.Component;

/**
 * Checks whether the user has all data access.
 */
@Component
public class DataAccessXapiAuthorization extends AbstractXapiAuthorization {
    @Override
    protected boolean checkImpl() {
        return Groups.hasAllDataAccess(getUser());
    }

    @Override
    protected boolean considerGuests() {
        return false;
    }
}
