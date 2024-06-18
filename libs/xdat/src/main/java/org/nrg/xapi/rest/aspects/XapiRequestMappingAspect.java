package org.nrg.xapi.rest.aspects;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.jetbrains.annotations.NotNull;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xapi.XapiUtils;
import org.nrg.xapi.authorization.XapiAuthorization;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotAuthenticatedException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.*;

import static org.nrg.xdat.security.helpers.AccessLevel.*;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;

/**
 * The aspect to handle the {@link XapiRequestMapping} annotation.
 */
@Aspect
@Component
@Slf4j
public class XapiRequestMappingAspect {
    @Autowired
    public XapiRequestMappingAspect(final SiteConfigPreferences preferences, final List<XapiAuthorization> authorizers) {
        _preferences = preferences;
        for (final XapiAuthorization authorizer : authorizers) {
            _authorizers.put(authorizer.getClass(), authorizer);
        }
        _realm = XapiUtils.getWwwAuthenticateBasicHeaderValue(_preferences.getSiteId());
    }

    public void setOpenUrls(final List<String> openUrls) {
        _openUrls.addAll(openUrls);
    }

    public void setAdminUrls(final List<String> adminUrls) {
        _adminUrls.addAll(adminUrls);
    }

    @Pointcut("execution(* org.nrg.xapi.rest.AbstractXapiRestController+.*(..)) && @annotation(xapiRequestMapping)")
    public void xapiRequestMappingPointcut(final XapiRequestMapping xapiRequestMapping) {
    }

    // TODO: Optimally, this method would be annotated with @Before and throw InsufficientPrivilegesException or
    // TODO: NotAuthenticatedException in the appropriate context. That exception would handled by
    // TODO: XapiRestControllerAdvice. That works with Spring 4.3.6 but not with Spring 4.2.9. This works around that by
    // TODO: setting the response status (for both situations) and the WWW-Authenticate header for the latter. Basically
    // TODO: the processXapiRequest() method can have the annotation and signature below, with the innards of the
    // TODO: evaluate() method becoming the body of processXapiRequest(). One caveat is that we may want to retain the
    // TODO: @Around structure just to keep the stopwatch, but the direct response write should go away.
    // TODO: @Before(value = "xapiRequestMappingPointcut(xapiRequestMapping)", argNames = "joinPoint,xapiRequestMapping")
    // TODO: public void processXapiRequest(final JoinPoint joinPoint, final XapiRequestMapping xapiRequestMapping) {
    @Around(value = "xapiRequestMappingPointcut(xapiRequestMapping)", argNames = "joinPoint,xapiRequestMapping")
    public Object processXapiRequest(final ProceedingJoinPoint joinPoint, final XapiRequestMapping xapiRequestMapping) throws Throwable {
        final HttpServletRequest request = getRequest();
        try {
            evaluate(joinPoint, xapiRequestMapping);

            final StopWatch stopWatch = log.isDebugEnabled() ? StopWatch.createStarted() : null;
            try {
                return joinPoint.proceed();
            } finally {
                if (stopWatch != null) {
                    stopWatch.stop();
                    log.debug("Request to {} took {} ms to execute", getRequestPath(request), NumberFormat.getInstance().format(stopWatch.getTime()));
                }
            }
        } catch (InsufficientPrivilegesException e) {
            final HttpServletResponse response = getResponse();
            response.setStatus(HttpStatus.FORBIDDEN.value());
            final UserI user = XDAT.getUserDetails();
            AccessLogger.LogResourceAccess(user != null ? user.getUsername() : "unknown", request, AccessLogger.getFullRequestUrl(request), FORBIDDEN);
        } catch (NotAuthenticatedException e) {
            final HttpServletResponse response = getResponse();
            response.setStatus(UNAUTHORIZED_VALUE);
            response.setHeader(WWW_AUTHENTICATE, _realm);
            final UserI user = XDAT.getUserDetails();
            AccessLogger.LogResourceAccess(user != null ? user.getUsername() : "unknown", request, AccessLogger.getFullRequestUrl(request), UNAUTHORIZED);
        }
        // If the return type is a primitive, this can't return null or Spring will complain, so return default value
        // for the primitive type. If the return type isn't a primitive, it's not in the default values map and will
        // return null, which is what we want.
        return DEFAULT_VALUES.get(((MethodSignature) joinPoint.getSignature()).getReturnType());
    }

    private void evaluate(final JoinPoint joinPoint, final XapiRequestMapping xapiRequestMapping) throws InsufficientPrivilegesException, NotAuthenticatedException, NotFoundException {
        final HttpServletRequest request    = getRequest();
        final String             requestUrl = AccessLogger.getFullRequestUrl(request);

        final UserI user = XDAT.getUserDetails();
        if (user == null) {
            AccessLogger.LogResourceAccess("", request, requestUrl, UNAUTHORIZED);
            throw new NotAuthenticatedException("User principal couldn't be found.");
        }

        final String username = user.getUsername();
        final String path     = getRequestPath(request);

        // Is restrictTo configured?
        final AccessLevel accessLevel = xapiRequestMapping.restrictTo();

        // We just let Null and GETs to open URLs go.
        if (accessLevel == Null || (isOpenUrl(path) && StringUtils.equalsIgnoreCase("GET", request.getMethod()))) {
            AccessLogger.LogResourceAccess(username, request, requestUrl);
            return;
        }

        // If access level is not null or Read (which could be valid when system is open and project is public), i.e.
        // authenticated or above, first check whether the user is logged in.
        if (user.isGuest() && _preferences.getRequireLogin() && accessLevel != Read) {
            log.info("Guest user from IP {} tried to access a URL that required authentication access: {}", AccessLogger.GetRequestIp(request), requestUrl);
            AccessLogger.LogResourceAccess(username, request, requestUrl, UNAUTHORIZED);
            throw new NotAuthenticatedException(requestUrl);
        }

        final XapiAuthorization authorizer;
        // This is for backwards compatibility.
        if (isAdminUrl(path) || isInitUrl(path)) {
            authorizer = _authorizers.get(Admin.getAuthClass());
        } else if (accessLevel == Authorizer) {
            final Method       method     = ((MethodSignature) joinPoint.getSignature()).getMethod();
            final AuthDelegate annotation = method.getAnnotation(AuthDelegate.class);
            if (annotation == null) {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The restrictTo was set to Authorizer, but no AuthDelegate annotation was found on the method " + method.getName());
            }
            final Class<? extends XapiAuthorization> delegateClass = annotation.value();
            if (!_authorizers.containsKey(delegateClass)) {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "The AuthDelegate specified the authorizer class " + delegateClass.getName() + " on the method " + method.getName() + " but no instance of that class was found.");
            }
            authorizer = _authorizers.get(delegateClass);
        } else {
            authorizer = _authorizers.get(accessLevel.getAuthClass());
        }

        authorizer.check(accessLevel, joinPoint, user, request);
        AccessLogger.LogResourceAccess(username, request, requestUrl);
    }

    @NotNull
    private String getRequestPath(final HttpServletRequest request) {
        return request.getServletPath() + StringUtils.defaultIfBlank(request.getPathInfo(), "");
    }

    private boolean isOpenUrl(final String path) {
        return checkUrl(path, _openUrls);
    }

    private boolean isAdminUrl(final String path) {
        return checkUrl(path, _adminUrls);
    }

    private boolean isInitUrl(final String path) {
        return checkUrl(path, _initUrls);
    }

    private boolean checkUrl(final String path, final Collection<String> urls) {
        return urls.stream().anyMatch(url -> PATH_MATCHER.match(url, path));
    }

    private static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    private static HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
    }

    private static final AntPathMatcher        PATH_MATCHER       = new AntPathMatcher();
    private static final String                UNAUTHORIZED       = HttpStatus.UNAUTHORIZED.toString();
    private static final int                   UNAUTHORIZED_VALUE = HttpStatus.UNAUTHORIZED.value();
    private static final String                FORBIDDEN          = HttpStatus.FORBIDDEN.toString();
    private static final Map<Class<?>, Object> DEFAULT_VALUES     = ImmutableMap.<Class<?>, Object>builder()
                                                                                .put(byte.class, (byte) 0)
                                                                                .put(short.class, (short) 0)
                                                                                .put(int.class, 0)
                                                                                .put(long.class, 0L)
                                                                                .put(float.class, 0.0f)
                                                                                .put(double.class, 0.0d)
                                                                                .put(boolean.class, false)
                                                                                .put(char.class, '\u0000')
                                                                                .build();

    private final SiteConfigPreferences _preferences;
    private final String                _realm;

    private final Map<Class<? extends XapiAuthorization>, XapiAuthorization> _authorizers = new HashMap<>();
    private final List<String>                                               _openUrls    = new ArrayList<>();
    private final List<String>                                               _adminUrls   = new ArrayList<>();
    private final List<String>                                               _initUrls    = new ArrayList<>();
}
