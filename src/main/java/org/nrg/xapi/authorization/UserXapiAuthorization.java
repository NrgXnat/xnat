package org.nrg.xapi.authorization;

import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.security.helpers.Roles;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.nrg.framework.exceptions.NrgServiceError.Instantiation;

/**
 * Checks whether the user is a site administrator.
 */
@Component
public class UserXapiAuthorization extends AbstractXapiAuthorization {
    @Override
    protected boolean checkImpl() {
        // Admins can access all user-scoped calls.
        if (Roles.isSiteAdmin(getUser())) {
            return true;
        }
        final List<String> usernames = getUsernames(getJoinPoint());
        if (usernames.isEmpty()) {
            throw new NrgServiceRuntimeException(Instantiation, "The User role was specified for access, but no users are indicated by the Username annotation.");
        }
        return usernames.contains(getUsername());
    }

    @Override
    protected boolean considerGuests() {
        return false;
    }
}
