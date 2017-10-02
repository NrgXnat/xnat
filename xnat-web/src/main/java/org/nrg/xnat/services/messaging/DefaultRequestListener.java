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
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DefaultRequestListener {
    @JmsListener(destination = "default")
    public void onRequest(final Object request) {
        log.warn("The default request listener received a request of type: {}. I don't know what to do with this. Please check your JMS/queuing configuration.", request.getClass().getName());
    }
}
