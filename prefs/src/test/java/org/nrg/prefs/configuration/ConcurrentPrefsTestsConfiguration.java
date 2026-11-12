/*
 * prefs: org.nrg.prefs.configuration.ConcurrentPrefsTestsConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@Import(PreferenceServiceTestsConfiguration.class)
@ComponentScan({"org.nrg.prefs.tools.basic", "org.nrg.prefs.tools.concurrent"})
public class ConcurrentPrefsTestsConfiguration {
}
