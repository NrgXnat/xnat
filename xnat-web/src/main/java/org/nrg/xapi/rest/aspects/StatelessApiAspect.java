package org.nrg.xapi.rest.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.springframework.stereotype.Component;
import org.aspectj.lang.annotation.Aspect;
import org.nrg.framework.annotations.StatelessApi;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.nrg.xdat.security.helpers.Users;
import org.springframework.security.core.context.SecurityContextHolder;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.security.UserI;

@Slf4j
@Aspect
@Component
public class StatelessApiAspect {

    @Pointcut("within(@org.nrg.framework.annotations.StatelessApi *)")
    public void beanAnnotatedWithStatelessApi() {}

    @Pointcut("execution(public * *(..))")
    public void publicMethod() {}


    @Pointcut("publicMethod() && beanAnnotatedWithStatelessApi()")
    public void statelessApiPointcut() {}

    @Around(value = "statelessApiPointcut()", argNames = "joinPoint")
    public Object processStatelessServletPointcut(final ProceedingJoinPoint joinPoint) throws Throwable {
        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        final HttpSession        session = request.getSession();

        try {
            return joinPoint.proceed();
        } finally {
            if (session != null) {
                UserI sessionUser = Users.getUserPrincipal(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
                if (sessionUser != null && Roles.checkRole(sessionUser, ROLE_POLLING_PROCESS)) {
                    session.invalidate();
                }
            }
        }
    }

    public final static String ROLE_POLLING_PROCESS = "MetricsApiAccessUser";

}