package org.nrg.xapi.rest.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.rest.ProjectId;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.helpers.AccessLevel;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The aspect to handle the {@link XapiRequestMapping} annotation.
 */
@Aspect
@Component
public class XapiRequestMappingAspect {
    @Pointcut("execution(* org.nrg.xapi.rest.AbstractXapiRestController+.*(..)) && @annotation(xapiRequestMapping)")
    public void xapiRequestMappingPointcut(final XapiRequestMapping xapiRequestMapping) {
    }

    @Before(value = "xapiRequestMappingPointcut(xapiRequestMapping)", argNames = "joinPoint,xapiRequestMapping")
    public void logAccess(final JoinPoint joinPoint, final XapiRequestMapping xapiRequestMapping) throws InitializationException, InsufficientPrivilegesException {
        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        final UserI user = XDAT.getUserDetails();
        if (user == null) {
            throw new InitializationException("User principal couldn't be found.");
        }

        final String username = user.getUsername();

        final String requestIp = AccessLogger.GetRequestIp(request);
        final String requestMethod = request.getMethod();
        final String requestUrl = request.getRequestURL().toString();
        _log.debug("User {} from IP {} requesting {} operation on URL {}", username, requestIp, requestMethod, requestUrl);
        _accessLog.error("{} {} {} {}", username, requestIp, requestMethod, requestUrl);

        // Is restrictTo configured?
        final AccessLevel accessLevel = xapiRequestMapping.restrictTo();

        switch (accessLevel) {
            case Null:
                // If not, we're done here.
                return;

            case Admin:
                // If access level is administrator, all we need to do is check whether this user is an administrator.
                if (!Roles.isSiteAdmin(user)) {
                    _log.info("User {} from IP {} tried to access a URL that required admin access: {}", username, requestIp, requestUrl);
                    throw new InsufficientPrivilegesException(username);
                }
                return;

            case Authenticated:
                // If access level is authenticated, all we need to do is check whether the user is logged in.
                if (user.isGuest()) {
                    _log.info("Guest user from IP {} tried to access a URL that required authentication access: {}", requestIp, requestUrl);
                    throw new InsufficientPrivilegesException(username);
                }
                return;
        }

        // Access level is specified, but the remaining access levels require that we be able to to determine the
        // current project, so let's find the project ID. We can inspect the method directly to find a parameter
        // annotated with the @ProjectId annotation.
        final List<String> projectIds = getProjectIds(joinPoint);
        for (final String projectId : projectIds) {
            testAccess(user, projectId, accessLevel);
        }
    }

    private List<String> getProjectIds(final JoinPoint joinPoint) {
        final Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        final int parameterIndex = getProjectIdParameterIndex(method);
        if (parameterIndex == -1) {
            return NO_PROJECT_IDS;
        }
        final Object candidate = joinPoint.getArgs()[parameterIndex];
        if (candidate instanceof String) {
            return Collections.singletonList((String) candidate);
        }
        if (candidate instanceof List) {
            //noinspection unchecked
            return (List<String>) candidate;
        }
        if (candidate instanceof Map) {
            final Map map = (Map) candidate;
            if (map.containsKey("projectIds")) {
                //noinspection unchecked
                return (List<String>) map.get("projectIds");
            }
            if (map.containsKey("projectId")) {
                return Collections.singletonList((String) map.get("projectId"));
            }
        }
        throw new RuntimeException("Found parameter " + parameterIndex + " annotated with @ProjectId for the method " + method.getName() + " but the annotated parameter is not a String, List of strings, or a map containing a key named projectId or projectIds.");
    }

    private void testAccess(final UserI user, final String projectId, final AccessLevel accessLevel) throws InsufficientPrivilegesException, InitializationException {
        try {
            if (!Permissions.hasAccess(user, projectId, accessLevel)) {
                throw new InsufficientPrivilegesException(user.getUsername(), projectId);
            }
        } catch (Exception e) {
            if (e instanceof InsufficientPrivilegesException) {
                throw (InsufficientPrivilegesException) e;
            }
            throw new InitializationException(e);
        }
    }

    private static int getProjectIdParameterIndex(final Method method) {
        final Annotation[][] annotations = method.getParameterAnnotations();
        for (int parameterIndex = 0; parameterIndex < annotations.length; parameterIndex++) {
            final Annotation[] parameterAnnotations = annotations[parameterIndex];
            if (parameterAnnotations.length == 0) {
                continue;
            }
            for (final Annotation instance : parameterAnnotations) {
                if (instance instanceof ProjectId) {
                    return parameterIndex;
                }
            }
        }
        return -1;
    }

    private static final Logger       _accessLog     = LoggerFactory.getLogger(AccessLogger.class);
    private static final Logger       _log           = LoggerFactory.getLogger(XapiRequestMappingAspect.class);
    private static final List<String> NO_PROJECT_IDS = Collections.emptyList();
}
