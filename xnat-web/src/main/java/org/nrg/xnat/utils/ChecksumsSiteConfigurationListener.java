/*
 * web: org.nrg.xnat.utils.ChecksumsSiteConfigurationListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import org.apache.commons.lang3.StringUtils;
import org.nrg.config.interfaces.SiteConfigurationPropertyChangedListener;

public class ChecksumsSiteConfigurationListener implements SiteConfigurationPropertyChangedListener {
    @Override
    public void siteConfigurationPropertyChanged(final String propertyName, final String newPropertyValue) {
        if (!StringUtils.isBlank(newPropertyValue) && (Boolean.TRUE.toString().equalsIgnoreCase(newPropertyValue) || Boolean.FALSE.toString().equalsIgnoreCase(newPropertyValue))) {
            CatalogUtils.setChecksumConfiguration(Boolean.parseBoolean(newPropertyValue));
        }
    }
}
