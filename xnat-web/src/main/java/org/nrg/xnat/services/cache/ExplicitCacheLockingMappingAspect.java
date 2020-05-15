package org.nrg.xnat.services.cache;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.nrg.xapi.authorization.AbstractXapiAuthorization;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static net.sf.ehcache.concurrent.LockType.READ;

/**
 * The aspect to handle the {@link CacheLock} annotation.
 */
@Aspect
@Component
@Slf4j
@Getter(PRIVATE)
public class ExplicitCacheLockingMappingAspect {
    @Pointcut("execution(* org.nrg.xnat.services.cache.AbstractXftItemAndCacheEventHandlerMethod+.*(..)) && @annotation(cacheLock)")
    public void cacheLockPointcut(final CacheLock cacheLock) {
    }

    @Around(value = "cacheLockPointcut(cacheLock)", argNames = "joinPoint, cacheLock")
    public Object handleCacheLockRequest(final ProceedingJoinPoint joinPoint, final CacheLock cacheLock) throws Throwable {
        final MethodSignature signature      = (MethodSignature) joinPoint.getSignature();
        final List<String>    parameterNames = Arrays.asList(signature.getParameterNames());
        final int             cacheIdIndex   = parameterNames.contains("cacheId") ? parameterNames.indexOf("cacheId") : AbstractXapiAuthorization.getAnnotatedParameterIndex(signature.getMethod(), CacheId.class);
        final Object          cacheId        = cacheIdIndex >= 0 ? joinPoint.getArgs()[cacheIdIndex] : null;
        final Cache           cache          = ((AbstractXftItemAndCacheEventHandlerMethod) joinPoint.getThis()).getEhCache();
        final boolean         isWriteLock     = cacheLock.write();

        if (cacheId != null) {
            if (isWriteLock) {
                cache.acquireWriteLockOnKey(cacheId);
            } else {
                cache.acquireReadLockOnKey(cacheId);
            }
        }
        try {
            return joinPoint.proceed();
        } finally {
            if (cacheId != null) {
                if (isWriteLock) {
                    cache.releaseWriteLockOnKey(cacheId);
                } else {
                    cache.releaseReadLockOnKey(cacheId);
                }
            }
        }
    }
}
