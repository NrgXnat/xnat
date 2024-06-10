package org.nrg.xnat.helpers.processing.handlers;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.services.messaging.processing.ProcessingOperationRequestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Service("processingOperationHandlerResolver")
@Slf4j
public class DefaultProcessingOperationHandlerResolver implements ProcessingOperationHandlerResolver {
    @Autowired
    public DefaultProcessingOperationHandlerResolver(final List<ProcessingOperationHandler> handlers) {
        _handlers = handlers;
    }

    @Override
    public List<ProcessingOperationHandler> getHandlers(final ProcessingOperationRequestData request) {
        final Class<? extends ProcessingOperationRequestData> requestType = request.getClass();
        log.debug("Searching for handler for processing requests of type {}", requestType);

        final List<ProcessingOperationHandler> handlers = _handlers.stream().filter(handler -> handler.handles(requestType)).collect(toList());
        if (handlers.isEmpty()) {
            log.warn("No handler found for request type " + requestType.getName() + ". Please check your classpath.");
            return Collections.emptyList();
        }
        if (log.isDebugEnabled()) {
            log.debug("Found {} handlers for request type {}: {}", handlers.size(), requestType, handlers.stream().map(Object::getClass).map(Class::getName).collect(joining(", ")));
        }
        return handlers;
    }

    private final List<ProcessingOperationHandler> _handlers;
}
