package org.nrg.xdat.rest;

import org.apache.commons.collections.ListUtils;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.security.UserI;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Provides basic functions for integrating Spring REST controllers with XNAT.
 */
public abstract class AbstractXnatRestApi {
    /**
     * Indicates whether the user is permitted to access a particular REST function. Access is granted if the user is a
     * site administrator <i>or</i> the user's login name matches one of the submitted <b>id</b> values. The latter case
     * is useful to test whether a user can edit the corresponding user account or is an owner of a project, for
     * example.
     *
     * @param ids    One or more IDs that can be tested against the username.
     *
     * @return Returns null if the user is permitted to access the API, otherwise it returns an error status code.
     */
    protected HttpStatus isPermitted(final String... ids) {
        final UserI user = getSessionUser();
        if (user == null) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (Arrays.asList(ids).contains(user.getUsername()) || Roles.isSiteAdmin(user)) {
            return null;
        }
        return HttpStatus.FORBIDDEN;
    }

    /**
     * Indicates whether the user is permitted to access a particular REST function. Access is granted if the user is a
     * site administrator <i>or</i> the user's login name matches one of the submitted <b>id</b> values. The latter case
     * is useful to test whether a user can edit the corresponding user account or is an owner of a project, for
     * example.
     *
     * @param roles    One or more roles that can be tested against the user.
     *
     * @return Returns null if the user is permitted to access the API, otherwise it returns an error status code.
     */
    protected HttpStatus hasPermittedRole(final String... roles) {
        final UserI user = getSessionUser();
        if (user == null) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (ListUtils.intersection(Arrays.asList(roles), new ArrayList<>(Roles.getRoles(user))).size() > 0 || Roles.isSiteAdmin(user)) {
            return null;
        }
        return HttpStatus.FORBIDDEN;
    }

    /**
     * Retrieves the user object for the current session.
     * @return The user object for the current session, or null if the user object can't be found.
     */
    protected UserI getSessionUser() {
        final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if ((principal instanceof UserI)) {
            return (UserI) principal;
        }
        return null;
    }
}
