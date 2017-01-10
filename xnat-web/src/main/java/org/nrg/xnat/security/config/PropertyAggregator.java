/*
 * web: org.nrg.xnat.security.config.PropertyAggregator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

public class PropertyAggregator extends Properties {

    public PropertyAggregator(final List<String> fileNames) {
        // Because our properties files aren't on the class path, we basically have to climb UP the folders for the
        // package containing this class, then two more folders for WEB-INF/classes, and then back down to the location
        // of the actual property file you want loaded. Suboptimal.
        final String pathOffset = StringUtils.repeat("../", getClass().getPackage().getName().split("\\.").length + 2);
        if (_log.isDebugEnabled()) {
            _log.debug("Found path offset of: " + pathOffset);
        }

        for (final String filename : fileNames) {
            final String path = pathOffset + filename;
            if (_log.isDebugEnabled()) {
                _log.debug("Now trying to load properties file from path: " + path);
            }
            URL url = getClass().getResource(path);
            if (url != null) {
                try {
                    load(url.openStream());
                } catch (IOException e) {
                    _log.error("Exception trying to open file " + filename + " from path " + url.getPath(), e);
                }
            }
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(PropertyAggregator.class);
}
