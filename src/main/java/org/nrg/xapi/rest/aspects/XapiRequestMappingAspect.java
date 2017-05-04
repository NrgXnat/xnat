package org.nrg.xapi.rest.aspects;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * The aspect to handle the {@link XapiRequestMapping} annotation.
 */
@Aspect
@Component
public class XapiRequestMappingAspect {
    @Before("@annotation(xapiRequestMapping)")
    public void logAccess(final XapiRequestMapping xapiRequestMapping) throws InitializationException, InsufficientPrivilegesException {
        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        final UserI user = XDAT.getUserDetails();
        if (user == null) {
            throw new InitializationException("User principal couldn't be found.");
        }

        final String username = user.getUsername();

        final String requestIp = AccessLogger.GetRequestIp(request);
        final String method = request.getMethod();
        final String requestUrl = request.getRequestURL().toString();
        _log.debug("User {} from IP {} requesting {} operation on URL {}", username, requestIp, method, requestUrl);
        _accessLog.error("{} {} {} {}", username, requestIp, method, requestUrl);

        // Is restrictTo configured?
        final String accessLevel = xapiRequestMapping.restrictTo();

        // If not, access is unrestricted, allow through.
        if (StringUtils.isBlank(accessLevel)) {
            return;
        }

        // There is restricted access level set, so does this user meet the criteria? Start with admin.
        if (StringUtils.equals("admin", accessLevel)) {
            if (!Roles.isSiteAdmin(user)) {
                _log.info("User {} from IP {} tried to access a URL that required admin access: {}", username, requestIp, requestUrl);
                throw new InsufficientPrivilegesException(username);
            }
            return;
        }

        // If authenticated required but user is guest, deny.
        if (StringUtils.equals("authenticated", accessLevel)) {
            if (user.isGuest()) {
                _log.info("Guest user from IP {} tried to access a URL that required authentication access: {}", requestIp, requestUrl);
                throw new InsufficientPrivilegesException(username);
            }
        }
        // read, edit, own require project ID and are checked in separate handler, so just return from here.
    }

    @Before("@annotation(xapiRequestMapping) && args(projectId,..)")
    public void checkProjectAccess(final XapiRequestMapping xapiRequestMapping, final String projectId) throws InitializationException, InsufficientPrivilegesException {
        final UserI user = XDAT.getUserDetails();
        if (user == null) {
            throw new InitializationException("User principal couldn't be found.");
        }

        final String accessLevel = xapiRequestMapping.restrictTo();

        try {
            if ((StringUtils.equals("read", accessLevel) && !Permissions.canReadProject(user, projectId)) ||
                (StringUtils.equals("edit", accessLevel) && !Permissions.canEditProject(user, projectId)) ||
                (StringUtils.equals("own", accessLevel) && !Permissions.ownsProject(user, projectId))) {
                // If not, throw an insufficient privileges exception.
                throw new InsufficientPrivilegesException(user.getUsername(), projectId);
            }
        } catch (Exception e) {
            if (e instanceof InsufficientPrivilegesException) {
                throw (InsufficientPrivilegesException) e;
            }
            throw new InitializationException(e);
        }
    }

    private static final Logger _accessLog = LoggerFactory.getLogger(AccessLogger.class);
    private static final Logger _log       = LoggerFactory.getLogger(XapiRequestMappingAspect.class);
}
