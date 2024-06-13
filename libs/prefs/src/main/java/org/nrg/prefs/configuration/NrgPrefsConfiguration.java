/*
 * prefs: org.nrg.prefs.configuration.NrgPrefsConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"org.nrg.prefs.services.impl", "org.nrg.prefs.repositories", "org.nrg.prefs.transformers"})
public class NrgPrefsConfiguration {
}
