/*
 * web: org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequestListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.messaging.prearchive;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.messaging.JmsRequestListener;
import org.nrg.xnat.helpers.prearchive.handlers.PrearchiveOperationHandler;
import org.nrg.xnat.helpers.prearchive.handlers.PrearchiveOperationHandlerResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PrearchiveOperationRequestListener implements JmsRequestListener<PrearchiveOperationRequest> {
    @Autowired
    public PrearchiveOperationRequestListener(final PrearchiveOperationHandlerResolver resolver) {
        _resolver = resolver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JmsListener(id = "prearchiveOperationRequest", destination = "prearchiveOperationRequest")
    public void onRequest(final PrearchiveOperationRequest request) {
        log.info("Now handling request: {}", request);
        final PrearchiveOperationHandler handler = _resolver.getHandler(request);
        try {
            log.debug("Received request from user {} to perform {} operation on prearchive session at: {}", request.getUsername(), request.getOperation(), request.getSessionData().getExternalUrl());
            handler.execute();
        } catch (Exception e) {
            log.error("An error occurred processing a request from user {} to perform {} operation on prearchive session at: {}", request.getUsername(), request.getOperation(), request.getSessionData().getExternalUrl(), e);
        }
    }

    private final PrearchiveOperationHandlerResolver _resolver;
}
