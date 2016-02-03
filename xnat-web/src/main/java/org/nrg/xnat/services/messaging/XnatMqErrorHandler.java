package org.nrg.xnat.services.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ErrorHandler;

public class XnatMqErrorHandler implements ErrorHandler {
    @Override
    public void handleError(Throwable throwable) {
        _log.error("An error occurred in the XNAT MQ handling", throwable);
    }

    private static final Logger _log = LoggerFactory.getLogger(XnatMqErrorHandler.class);
}
