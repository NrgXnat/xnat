/*
 * config: org.nrg.config.exceptions.SiteConfigurationFileNotFoundException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config.exceptions;

import java.io.FileNotFoundException;
import java.util.List;

public final class SiteConfigurationFileNotFoundException extends SiteConfigurationException {
    public SiteConfigurationFileNotFoundException(final FileNotFoundException exception) {
        super("Couldn't find a configuration file", exception);
    }

    public SiteConfigurationFileNotFoundException(String configFileName, List<String> configFilesLocations) {
        super(String.format("The file '%s' was not found in any of the following locations:\n%s", configFileName, String.join("\n", configFilesLocations)));
    }
}
