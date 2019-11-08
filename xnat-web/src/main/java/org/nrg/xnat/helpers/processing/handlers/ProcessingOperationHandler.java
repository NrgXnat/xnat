package org.nrg.xnat.helpers.processing.handlers;

import org.nrg.xnat.services.messaging.processing.ProcessingOperationRequestData;

/**
 * Defines the interface for a class that can handle a particular processing operation.
 */
public interface ProcessingOperationHandler {
    /**
     * Executes the processing operation.
     */
    void execute(final ProcessingOperationRequestData request);

    /**
     * Indicates whether this implementation can handle the type of request object.
     *
     * This may also be specified with the {@link Processes} annotation.
     *
     * @param requestType The type of request object.
     *
     * @return Returns true if this handler can handle the request type.
     */
    boolean handles(final Class<? extends ProcessingOperationRequestData> requestType);
}
