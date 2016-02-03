package org.nrg.xnat.services.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRequestListener {
    public void onRequest(final Object request) throws Exception {
        if (_log.isDebugEnabled()) {
            _log.debug("Just received a request of type: " + request.getClass().getName());
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(DefaultRequestListener.class);
}
