/*
 * web: org.nrg.xnat.services.messaging.prearchive.PrearchiveOperationRequestListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.messaging.prearchive;

import org.nrg.xnat.helpers.prearchive.handlers.AbstractPrearchiveOperationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrearchiveOperationRequestListener {

    @SuppressWarnings("unused")
    public void onRequest(final PrearchiveOperationRequest request) {
        final AbstractPrearchiveOperationHandler handler = AbstractPrearchiveOperationHandler.getHandler(request);
        try {
            if (_log.isDebugEnabled()) {
                _log.debug("Received request from user {} to perform {} operation on prearchive session at: {}", request.getUser().getUsername(), request.getOperation(), request.getSessionData().getExternalUrl());
            }
            handler.execute();
        } catch (Exception e) {
            final String message = String.format("An error occurred processing a request from user %s to perform %s operation on prearchive session at: %s", request.getUser().getUsername(), request.getOperation(), request.getSessionData().getExternalUrl());
            _log.error(message, e);
        }
    }

    private final static Logger _log = LoggerFactory.getLogger(PrearchiveOperationRequestListener.class);
}
