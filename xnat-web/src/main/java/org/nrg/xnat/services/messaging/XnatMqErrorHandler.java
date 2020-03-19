/*
 * web: org.nrg.xnat.services.messaging.XnatMqErrorHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

// @Component
@Slf4j
public class XnatMqErrorHandler implements ErrorHandler {
    @Override
    public void handleError(final Throwable throwable) {
        log.error("An error occurred in the XNAT MQ handling", throwable);
    }
}
