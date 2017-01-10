/*
 * web: org.nrg.xnat.services.messaging.XnatMqErrorHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

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
