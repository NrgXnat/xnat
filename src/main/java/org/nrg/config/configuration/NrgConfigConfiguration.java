/*
 * org.nrg.config.configuration.NrgConfigConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config.configuration;

import org.nrg.prefs.configuration.NrgPrefsConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(NrgPrefsConfiguration.class)
@ComponentScan({"org.nrg.config.daos", "org.nrg.config.services.impl", "org.nrg.prefs.services.impl.hibernate"})
public class NrgConfigConfiguration {
}
