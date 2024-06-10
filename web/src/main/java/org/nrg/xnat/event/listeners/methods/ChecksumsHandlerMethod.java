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
import org.nrg.xnat.utils.CatalogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChecksumsHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    @Autowired
    public ChecksumsHandlerMethod() {
        super(CHECKSUMS);
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        if (StringUtils.equals(CHECKSUMS, preference)) {
            CatalogUtils.setChecksumConfiguration(Boolean.parseBoolean(value));
        }
    }

    private static final String CHECKSUMS = "checksums";
}
