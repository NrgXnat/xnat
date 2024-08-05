package org.nrg.xnat.web.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.security.UserI;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.context.request.async.CallableProcessingInterceptor;

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Slf4j
public class AsyncLifecycleMonitor implements CallableProcessingInterceptor {
    @Override
    public <T> void beforeConcurrentHandling(final NativeWebRequest request, final Callable<T> task) {
        logLifecycleEvent("beforeConcurrentHandling", request, task);
    }

    @Override
    public <T> void preProcess(final NativeWebRequest request, final Callable<T> task) {
        logLifecycleEvent("preProcess", request, task);
    }

    @Override
    public <T> void postProcess(final NativeWebRequest request, final Callable<T> task, final Object concurrentResult) {
        logLifecycleEvent("postProcess", request, task, concurrentResult);
    }

    @Override
    public <T> Object handleTimeout(final NativeWebRequest request, final Callable<T> task) {
        logLifecycleEvent("handleTimeout", request, task);
        // This is what the Spring TimeoutCallableProcessingInterceptor class does
        return new AsyncRequestTimeoutException();
    }

    @Override
    public <T> void afterCompletion(final NativeWebRequest request, final Callable<T> task) {
        logLifecycleEvent("afterCompletion", request, task);
    }

    private <T> void logLifecycleEvent(final String event, final NativeWebRequest request, final Callable<T> task) {
        logLifecycleEvent(event, request, task, null);
    }

    private static <T> void logLifecycleEvent(final String event, final NativeWebRequest request, final Callable<T> task, final Object result) {
        final boolean isExceptionResult = result instanceof Exception;
        if (log.isDebugEnabled() || isExceptionResult) {
            final String renderedRequest = renderNativeWebRequest(request);
            final String taskClass       = task == null ? "<null task>" : task.getClass().getName();
            if (result == null) {
                log.debug("{}() - Request for task of type {}: {}", event, taskClass, renderedRequest);
            } else {
                if (isExceptionResult) {
                    log.error("An exception occurred during asynchronous processing. Request for task of type {}: {}", taskClass, renderedRequest, ObjectUtils.defaultIfNull(((Exception) result).getCause(), (Exception) result));
                } else {
                    log.debug("{}() - Request for task of type {}: {}, result type {}", event, renderedRequest, taskClass, result.getClass().getName());
                }
            }
        }
    }

    private static String renderNativeWebRequest(final NativeWebRequest request) {
        if (request == null) {
            return "<null native request container>";
        }
        final Object nativeRequest = request.getNativeRequest();
        if (nativeRequest == null) {
            return "<null native request>";
        }
        final Class<?> nativeRequestClass = nativeRequest.getClass();
        if (javax.servlet.http.HttpServletRequestWrapper.class.isAssignableFrom(nativeRequestClass)) {
            final HttpServletRequestWrapper wrapper    = (HttpServletRequestWrapper) nativeRequest;
            final HttpSession               session    = wrapper.getSession();
            final String                    username   = getSessionUsername(session);
            final String                    requestURI = wrapper.getRequestURI();
            return "HTTP request to URI " + requestURI + " by user '" + username + "'";
        }
        if (org.apache.http.client.methods.HttpRequestWrapper.class.isAssignableFrom(nativeRequestClass)) {
            return "org.apache.http.client.methods.HttpRequestWrapper[" + nativeRequest.toString() + "]";
        }
        if (org.springframework.http.client.support.HttpRequestWrapper.class.isAssignableFrom(nativeRequestClass)) {
            return "org.springframework.http.client.support.HttpRequestWrapper[" + nativeRequest.toString() + "]";
        }
        return nativeRequestClass.getName() + ": " + nativeRequest.toString() + "; subclass of " + getHierarchy(nativeRequestClass);
    }

    private static String getHierarchy(final Class<?> subclass) {
        final List<String> superclasses = new ArrayList<>();
        Class<?>           superclass   = subclass;
        while ((superclass = superclass.getSuperclass()) != null) {
            superclasses.add(superclass.getName());
        }
        return StringUtils.join(superclasses, "->");
    }

    private static String getSessionUsername(final HttpSession session) {
        final Object contextCandidate = session.getAttribute(SPRING_SECURITY_CONTEXT_KEY);
        if (contextCandidate instanceof SecurityContext) {
            final SecurityContext context        = (SecurityContext) contextCandidate;
            final Authentication  authentication = context.getAuthentication();
            if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
                final Object userCandidate = authentication.getPrincipal();
                if (userCandidate instanceof UserI) {
                    return ((UserI) userCandidate).getUsername();
                }
                if (userCandidate instanceof String) {
                    return (String) userCandidate;
                }
            }
        }
        return "<unknown>";
    }
}
