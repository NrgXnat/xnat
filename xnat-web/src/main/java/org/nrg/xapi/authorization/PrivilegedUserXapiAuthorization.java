package org.nrg.xapi.authorization;

import org.aspectj.lang.JoinPoint;
import org.nrg.xapi.authorization.AbstractXapiAuthorization;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.entities.UserRole;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class PrivilegedUserXapiAuthorization extends AbstractXapiAuthorization {
    @Override
    protected boolean checkImpl(final AccessLevel accessLevel, final JoinPoint joinPoint, final UserI user, final HttpServletRequest request) {
        return Roles.isSiteAdmin(user) || Roles.checkRole(user, CustomFormsConstants.FORM_MANAGER_ROLE) || Roles.checkRole(user, UserRole.ROLE_PRIVILEGED);
    }

    @Override
    protected boolean considerGuests() {
        return false;
    }

}
