package org.nrg.xapi.authorization;

import org.aspectj.lang.JoinPoint;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.security.UserI;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.nrg.framework.exceptions.NrgServiceError.Instantiation;

/**
 * Checks whether the user is a site administrator.
 */
@Component
public class UserXapiAuthorization extends AbstractXapiAuthorization {
    @Override
    protected boolean checkImpl(final AccessLevel accessLevel, final JoinPoint joinPoint, final UserI user, final HttpServletRequest request) {
        // Admins can access all user-scoped calls.
        if (Roles.isSiteAdmin(user)) {
            return true;
        }
        final List<String> usernames = getUsernames(joinPoint);
        if (usernames.isEmpty()) {
            throw new NrgServiceRuntimeException(Instantiation, "The User role was specified for access, but no users are indicated by the Username annotation.");
        }
        return usernames.contains(user.getUsername());
    }

    @Override
    protected boolean considerGuests() {
        return false;
    }
}
