/*
 * web: org.nrg.xnat.services.messaging.DefaultRequestListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.messaging;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.messaging.JmsRequestListener;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DefaultRequestListener implements JmsRequestListener<Object> {
    /**
     * {@inheritDoc}
     */
    @Override
    @JmsListener(id = "defaultRequest", destination = "defaultRequest")
    public void onRequest(final Object request) {
        log.info("Now handling request of type {}: {}", request.getClass().getName(), request);
    }
}
