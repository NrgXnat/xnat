package org.nrg.xapi.rest;

import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.security.UserI;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Provides basic functions for integrating Spring REST controllers with XNAT.
 */
public abstract class AbstractXnatRestApi {
    protected HttpStatus isPermitted(String id) {
        UserI sessionUser = getSessionUser();
        if (sessionUser == null) {
            return HttpStatus.UNAUTHORIZED;
        }
        if ((sessionUser.getUsername().equals(id)) || (isPermitted() == null)) {
            return null;
        }
        return HttpStatus.FORBIDDEN;
    }

    protected UserI getSessionUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if ((principal instanceof UserI)) {
            return (UserI) principal;
        }
        return null;
    }

    protected HttpStatus isPermitted() {
        UserI sessionUser = getSessionUser();
        if ((sessionUser instanceof XDATUser)) {
            return ((XDATUser) sessionUser).isSiteAdmin() ? null : HttpStatus.FORBIDDEN;
        }

        return null;
    }
}
