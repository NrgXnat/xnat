/*
 * web: org.nrg.xnat.security.ResetEmailRequests
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import lombok.extern.slf4j.Slf4j;
import org.nrg.mail.services.EmailRequestLogService;
import org.nrg.xnat.task.AbstractXnatRunnable;

@Slf4j
public class ResetEmailRequests extends AbstractXnatRunnable {
    public ResetEmailRequests(final EmailRequestLogService service) {
        _service = service;
    }

    @Override
    protected void runTask() {
        if (_service != null) {
            _service.clearLogs();
        }
    }

    private final EmailRequestLogService _service;
}
