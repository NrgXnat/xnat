/*
 * web: org.nrg.xnat.initialization.ControllerConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization;

import org.nrg.xapi.configuration.RestApiConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(RestApiConfig.class)
public class ControllerConfig {
}
