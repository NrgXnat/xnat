package org.nrg.xnat.helpers.processing.handlers;

import org.nrg.xnat.services.messaging.processing.ProcessingOperationRequestData;

import java.util.List;

public interface ProcessingOperationHandlerResolver {
    List<ProcessingOperationHandler> getHandlers(final ProcessingOperationRequestData request);
}
