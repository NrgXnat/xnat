/*
 * web: org.nrg.xnat.services.messaging.processing.ProcessingOperationRequestListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.messaging.processing;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.messaging.JmsRequestListener;
import org.nrg.xnat.helpers.processing.handlers.ProcessingOperationHandler;
import org.nrg.xnat.helpers.processing.handlers.ProcessingOperationHandlerResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.joining;

@Component
@Slf4j
public class ProcessingOperationRequestListener implements JmsRequestListener<ProcessingOperationRequest<? extends ProcessingOperationRequestData>> {
    @Autowired
    public ProcessingOperationRequestListener(final ProcessingOperationHandlerResolver resolver) {
        _resolver = resolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JmsListener(id = "processingOperationRequest", destination = "processingOperationRequest")
    public void onRequest(final ProcessingOperationRequest<? extends ProcessingOperationRequestData> request) {
        final ProcessingOperationRequestData   data     = request.getRequestData();
        final List<ProcessingOperationHandler> handlers = _resolver.getHandlers(data);
        if (log.isDebugEnabled()) {
            log.debug("Found {} handlers to handle request from user {} to perform {} processing operation with parameters {}: {}", handlers.size(), data.getUsername(), data.getProcessingId(), data.getParameters(), handlers.stream().map(Object::getClass).map(Class::getName).collect(joining(", ")));
        }
        handlers.stream().parallel().forEach(handler -> handler.execute(data));
    }

    private final ProcessingOperationHandlerResolver _resolver;
}
