package org.nrg.xnat.micrometer.web.handler;

import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.micrometer.annotations.ApplyObservationInterceptor;
import org.nrg.xnat.micrometer.observation.handler.RequestObservationHandler;
import org.springframework.web.method.HandlerMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;

import static org.nrg.xnat.utils.XnatHttpUtils.REQUEST_START_INSTANT;

@Slf4j
@Component
public class XnatMicrometerHandlerInterceptorAdapter extends HandlerInterceptorAdapter {


    public XnatMicrometerHandlerInterceptorAdapter(final ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (isRequiredAnnotationPresent(handler)) {
            Instant requestStartTime = Instant.now();
            request.setAttribute(REQUEST_START_INSTANT, requestStartTime);
        }
        return super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        if (isRequiredAnnotationPresent(handler)) {
            new RequestObservationHandler(observationRegistry).doObserve(request, response, handler, modelAndView);
        }
    }

    private boolean isRequiredAnnotationPresent(final Object handler) {
        if (handler instanceof HandlerMethod) {
            return ((HandlerMethod)handler).getMethod().getDeclaringClass().isAnnotationPresent(ApplyObservationInterceptor.class);
        }
        return false;
    }

    private final ObservationRegistry observationRegistry;

}
