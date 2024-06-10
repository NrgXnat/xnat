/*
 * web: org.nrg.xnat.event.listeners.methods.PasswordExpirationHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.security.XnatExpiredPasswordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PasswordExpirationHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    @Autowired
    public PasswordExpirationHandlerMethod(final XnatExpiredPasswordFilter filter) {
        super("passwordExpirationType", "passwordExpirationInterval", "passwordExpirationDate");
        _filter = filter;
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        _filter.refreshFromSiteConfig();
    }

    private final XnatExpiredPasswordFilter _filter;
}
