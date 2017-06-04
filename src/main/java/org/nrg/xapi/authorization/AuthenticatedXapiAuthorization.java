package org.nrg.xapi.authorization;

import org.springframework.stereotype.Component;

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
    protected boolean checkImpl() {
        // This will actually never get hit because the considerGuests() bans guests in the first place.
        return true;
    }
}
