/*
 * web: org.nrg.xnat.initialization.ControllerConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.configuration.RestApiConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(RestApiConfig.class)
@Slf4j
public class ControllerConfig {
    public ControllerConfig() {
        log.info("Creating ControllerConfig");
    }
}

