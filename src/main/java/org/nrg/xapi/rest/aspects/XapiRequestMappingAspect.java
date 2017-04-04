package org.nrg.xapi.rest.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.XDAT;
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
    @Before("@annotation(org.nrg.xapi.rest.XapiRequestMapping)")
    public void logAccess(final JoinPoint joinPoint) throws Throwable {
        final HttpServletRequest request  = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        final UserI              user     = XDAT.getUserDetails();
        final String             username = user != null ? user.getLogin() : "unknown";

        _log.debug("User {} encountered the join point: {}", username, joinPoint.toLongString());
        _accessLog.error("{} {} {} {}", username, AccessLogger.GetRequestIp(request), request.getMethod(), request.getRequestURL().toString());
    }

    private static final Logger _accessLog = LoggerFactory.getLogger(AccessLogger.class);
    private static final Logger _log       = LoggerFactory.getLogger(XapiRequestMappingAspect.class);
}
