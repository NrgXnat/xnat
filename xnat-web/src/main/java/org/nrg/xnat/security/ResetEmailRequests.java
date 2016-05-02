/*
 * org.nrg.xnat.security.ResetEmailRequestsJob
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.security;

import org.nrg.mail.services.EmailRequestLogService;

import java.util.concurrent.Callable;

public class ResetEmailRequests implements Runnable {
    public ResetEmailRequests(final EmailRequestLogService service) {
        _service = service;
    }

    @Override
    public void run() {
        if (_service != null) {
            _service.clearLogs();
        }
    }

    private final EmailRequestLogService _service;
}