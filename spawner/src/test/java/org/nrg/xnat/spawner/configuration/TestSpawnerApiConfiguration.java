/*
 * spawner: org.nrg.xnat.spawner.configuration.TestSpawnerApiConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.configuration;

import org.nrg.framework.test.OrmTestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("org.nrg.xnat.spawner.rest")
@Import({SpawnerConfig.class, OrmTestConfiguration.class, BasicTestConfiguration.class})
public class TestSpawnerApiConfiguration {
}
