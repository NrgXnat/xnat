package org.nrg.xapi.rest.aspects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xapi.authorization.XapiAuthorization;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotAuthenticatedException;
import org.nrg.xapi.rest.AuthDelegate;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.nrg.xdat.security.helpers.AccessLevel.*;

/**
 * The aspect to handle the {@link XapiRequestMapping} annotation.
 */
@Aspect
@Component
public class XapiRequestMappingAspect {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    public XapiRequestMappingAspect(final SiteConfigPreferences preferences, final List<XapiAuthorization> authorizers) {
        _preferences = preferences;
        for (final XapiAuthorization authorizer : authorizers) {
            _authorizers.put(authorizer.getClass(), authorizer);
        }
    }

    @SuppressWarnings("unused")
    public void setOpenUrls(final List<String> openUrls) {
        _openUrls.addAll(openUrls);
    }

    @SuppressWarnings("unused")
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
    public Object processXapiRequest(final ProceedingJoinPoint joinPoint,
                                     final XapiRequestMapping xapiRequestMapping) throws Throwable {
        try {
            evaluate(joinPoint, xapiRequestMapping);

            final StopWatch stopWatch = _log.isDebugEnabled() ? new StopWatch() {{ start(); }} : null;
            try {
                return joinPoint.proceed();
            } finally {
                if (stopWatch != null) {
                    stopWatch.stop();
                    final HttpServletRequest request = getRequest();
                    _log.debug("Request to {} took {}ms to execute", request.getServletPath() + request.getPathInfo(), NumberFormat.getInstance().format(stopWatch.getTotalTimeMillis()));
                }
            }
        } catch (InsufficientPrivilegesException e) {
            final HttpServletResponse response = getResponse();
            response.setStatus(HttpStatus.FORBIDDEN.value());
        } catch (NotAuthenticatedException e) {
            final HttpServletResponse response = getResponse();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setHeader(WWW_AUTH_HEADER, "Basic realm=\"" + _preferences.getSiteId() + "\"");
        }
        return null;
    }

    private void evaluate(final JoinPoint joinPoint, final XapiRequestMapping xapiRequestMapping) throws InsufficientPrivilegesException, NotAuthenticatedException {
        final HttpServletRequest request = getRequest();
        final UserI              user    = XDAT.getUserDetails();
        if (user == null) {
            throw new InsufficientPrivilegesException("User principal couldn't be found.");
        }

        final String username = user.getUsername();

        final String requestIp     = AccessLogger.GetRequestIp(request);
        final String requestMethod = request.getMethod();
        final String requestUrl    = request.getRequestURL().toString();
        _log.debug("User {} from IP {} requesting {} operation on URL {}", username, requestIp, requestMethod, requestUrl);
        _accessLog.error("{} {} {} {}", username, requestIp, requestMethod, requestUrl);

        final String path = request.getServletPath() + request.getPathInfo();

        // Is restrictTo configured?
        final AccessLevel accessLevel = xapiRequestMapping.restrictTo();

        // We just let Null and open URLs go.
        if (accessLevel == Null || isOpenUrl(path)) {
            return;
        }

        // If access level is not null or Read (which could be valid when system is open and project is public), i.e.
        // authenticated or above, first check whether the user is logged in.
        if (user.isGuest() && _preferences.getRequireLogin() && accessLevel != Read) {
            _log.info("Guest user from IP {} tried to access a URL that required authentication access: {}", requestIp, requestUrl);
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
        for (final String url : urls) {
            if (PATH_MATCHER.match(url, path)) {
                return true;
            }
        }
        return false;
    }

    private static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    private static HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
    }


    private static final Logger         _accessLog      = LoggerFactory.getLogger(AccessLogger.class);
    private static final Logger         _log            = LoggerFactory.getLogger(XapiRequestMappingAspect.class);
    private static final AntPathMatcher PATH_MATCHER    = new AntPathMatcher();
    private static final String         WWW_AUTH_HEADER = "WWW-Authenticate";

    private final SiteConfigPreferences _preferences;

    private final Map<Class<? extends XapiAuthorization>, XapiAuthorization> _authorizers = Maps.newHashMap();
    private final List<String>                                               _openUrls    = Lists.newArrayList();
    private final List<String>                                               _adminUrls   = Lists.newArrayList();
    private final List<String>                                               _initUrls    = Lists.newArrayList();
}
