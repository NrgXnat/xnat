package org.nrg.xnat.helpers.processing.handlers;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.services.messaging.processing.ProcessingOperationRequestData;
import org.springframework.stereotype.Component;

/**
 * This provides a default implementation of the processing operation handler just to make sure all requests are logged and handled.
 */
@Component
@Slf4j
public class DefaultProcessingOperationHandler extends AbstractProcessingOperationHandler {
    protected DefaultProcessingOperationHandler(final XnatUserProvider userProvider) {
        super(userProvider);
    }

    @Override
    public void execute(final ProcessingOperationRequestData request) {
        log.debug("Got a request of type {}", request.getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handles(final Class<? extends ProcessingOperationRequestData> requestType) {
        // This handles all request types because it's just logging the request.
        log.debug("Asked if I could handle request of type: {}", requestType.getName());
        return true;
    }
}
