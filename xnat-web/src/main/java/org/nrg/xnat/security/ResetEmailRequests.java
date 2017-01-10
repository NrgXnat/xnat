/*
 * web: org.nrg.xnat.security.ResetEmailRequests
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
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
