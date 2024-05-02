package org.nrg.xnat.micrometer.observation.handler;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.transport.RequestReplyReceiverContext;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.web.http.AsyncLifecycleMonitor;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.nrg.xnat.utils.XnatHttpUtils.*;

@Slf4j
public class RequestObservationHandler {

    public RequestObservationHandler(final ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    public void doObserve(final HttpServletRequest request, final HttpServletResponse response, final Object handler,
                          final ModelAndView modelAndView) {

        final Object requestStartInstant = request.getAttribute(REQUEST_START_INSTANT);
        final int statusCode = response.getStatus();
        if (requestStartInstant != null && statusCode < 400) {
            final Instant start = (Instant)requestStartInstant;
            final Instant finish = Instant.now();
            final String requestURI = request.getRequestURI();
            final String userAgent = request.getHeader("User-Agent");
            final long responseTime = Duration.between(start, finish).toMillis();
            final String method = request.getMethod();
            final String userName = AsyncLifecycleMonitor.getSessionUsername(request.getSession());
            RequestReplyReceiverContext<HttpServletRequest, HttpServletResponse> receiverContext
                    = new RequestReplyReceiverContext<>(HttpServletRequest::getHeader);
            receiverContext.setCarrier(request);
            receiverContext.setResponse(response);
            doObserve(responseTime, requestURI, userAgent, method, statusCode, userName, receiverContext);
        }
    }

    public void doObserveError(final String requestUri,
                               final String method, final HttpStatus status,
                               final String userName,
                               @Nullable final String throwableClassName,
                               @Nullable final String userAgent,
                               @Nullable final String causeMessage) {
        Observation observation = Observation.createNotStarted(HTTP_SERVER_REQUESTS_ERROR_METRIC_NAME,  observationRegistry);
        observation.lowCardinalityKeyValue("method", method);
        observation.lowCardinalityKeyValue("status", String.valueOf(status.value()));
        if (userAgent != null) {
            observation.highCardinalityKeyValue("userAgent",userAgent);
        }
        observation.highCardinalityKeyValue("uri", requestUri);
        observation.highCardinalityKeyValue("now", String.valueOf(Instant.now()));
        observation.highCardinalityKeyValue("user", userName);
        if (throwableClassName != null) {
            observation.highCardinalityKeyValue("cause", throwableClassName);
        }
        if (causeMessage != null) {
            observation.highCardinalityKeyValue("message", causeMessage);
        }
        observation.observe(() -> log.error("HTTP status {}: Request by user {} to URL {} caused an exception of type {}{}", status, userName, requestUri, throwableClassName, defaultIfBlank(causeMessage, "")));
    }

    private void doObserve(final long responseTime, final String requestUri, final String userAgent,
                           final String method, final int statusCode,
                           final String userName,
                           final RequestReplyReceiverContext<HttpServletRequest, HttpServletResponse> receiverContext
                           ) {
        Observation observation = Observation.createNotStarted(HTTP_SERVER_REQUESTS_METRIC_NAME, () -> receiverContext, observationRegistry);
        observation.lowCardinalityKeyValue("method", method);
        observation.lowCardinalityKeyValue("status", String.valueOf(statusCode));
        if (userAgent != null) {
            observation.highCardinalityKeyValue("userAgent",userAgent);
        }
        observation.highCardinalityKeyValue("uri", requestUri);
        observation.highCardinalityKeyValue("responseTime", String.valueOf(responseTime));
        observation.highCardinalityKeyValue("user", userName);
        observation.observe(() -> log.info("Request Observation: {} ", tagsFrom(method, requestUri, statusCode, userName, responseTime)));
    }


    private  Iterable<Tag> tagsFrom(final String method, final String uri, final int status, final String userName, final long responseTime) {
        return Tags.of("method", method, "uri", uri, "status", String.valueOf(status), "user", userName, "response_time", String.valueOf(responseTime));
    }

    private final ObservationRegistry observationRegistry;

}
