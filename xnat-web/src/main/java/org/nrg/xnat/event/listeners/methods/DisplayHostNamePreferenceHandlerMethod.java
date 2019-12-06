/*
 * web: org.nrg.xnat.event.listeners.methods.ChecksumsHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.preferences.DisplayHostName;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DisplayHostNamePreferenceHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    @Autowired
    public DisplayHostNamePreferenceHandlerMethod(final XnatAppInfo appInfo) {
        super(PREFERENCES);
        _appInfo = appInfo;
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        if (StringUtils.equals(PREFERENCES, preference)) {
            log.debug("Setting display host name with value: {}", value);
            _appInfo.setDisplayHostName(DisplayHostName.valueOf(value));
        }
    }

    private static final String PREFERENCES = "displayHostName";

    private final XnatAppInfo _appInfo;
}
