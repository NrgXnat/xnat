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

@Slf4j
public class DefaultRequestListener {
    public void onRequest(final Object request) {
        log.info("Just received a request of type: {}", request.getClass().getName());
    }
}
