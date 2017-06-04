package org.nrg.xapi.authorization;

import org.aspectj.lang.reflect.MethodSignature;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xapi.rest.AuthorizedRoles;
import org.nrg.xdat.security.helpers.Roles;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Checks whether the user is a site administrator.
 */
@Component
public class RoleXapiAuthorization extends AbstractXapiAuthorization {
    @Override
    protected boolean checkImpl() {
        final Method          method = ((MethodSignature) getJoinPoint().getSignature()).getMethod();
        final AuthorizedRoles roles  = method.getAnnotation(AuthorizedRoles.class);
        if (roles == null) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The restrictTo was set to Role, but not AuthorizedRoles annotation was found on the method " + method.getName());
        }
        final Collection<String> userRoles       = Roles.getRoles(getUser());
        final List<String>       authorizedRoles = Arrays.asList(roles.value());
        userRoles.retainAll(authorizedRoles);
        return userRoles.size() > 0;
    }

    @Override
    protected boolean considerGuests() {
        // Technically you could consider guests because maybe one of the roles is ANONYMOUS, but that's silly.
        return false;
    }
}
