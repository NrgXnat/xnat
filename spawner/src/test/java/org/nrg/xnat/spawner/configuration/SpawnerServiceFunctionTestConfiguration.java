/*
 * spawner: org.nrg.xnat.spawner.configuration.SpawnerServiceFunctionTestConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan({"org.nrg.xnat.spawner.components", "org.nrg.xnat.spawner.services.impl.constants"})
@Import(BasicTestConfiguration.class)
public class SpawnerServiceFunctionTestConfiguration {
}
