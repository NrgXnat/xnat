/*
 * web: org.nrg.xnat.configuration.XnatServicesConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration manages configuration and instantiation of core XNAT/XDAT/XFT services.
 */
@Configuration
@ComponentScan({"org.nrg.xnat.services.archive.impl",
                "org.nrg.xnat.services.investigators",
                "org.nrg.xnat.services.system.impl.hibernate",
                "org.nrg.xnat.services.validation"})
public class XnatServicesConfig {
}
