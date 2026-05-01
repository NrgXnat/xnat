/*
 * prefs: org.nrg.prefs.configuration.PreferenceServiceTestsConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.configuration;

import org.nrg.framework.configuration.FrameworkConfig;
import org.nrg.framework.test.OrmTestConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({DefaultResolverConfiguration.class, FrameworkConfig.class, OrmTestConfiguration.class})
public class PreferenceServiceTestsConfiguration {
}
