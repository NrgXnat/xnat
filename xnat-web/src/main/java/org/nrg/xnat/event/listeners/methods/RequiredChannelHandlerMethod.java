/*
 * web: org.nrg.xnat.event.listeners.methods.RequiredChannelHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xnat.security.TranslatingChannelProcessingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RequiredChannelHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    @Autowired
    public RequiredChannelHandlerMethod(final TranslatingChannelProcessingFilter filter) {
        super("securityChannel");
        _filter = filter;
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        if (StringUtils.equals("securityChannel", preference)) {
            _filter.setRequiredChannel(value);
        }
    }

    private final TranslatingChannelProcessingFilter _filter;
}
