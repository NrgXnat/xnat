/*
 * framework: org.nrg.framework.services.TestPropertiesServiceConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.services;

import org.nrg.framework.configuration.FrameworkConfig;
import org.nrg.framework.test.OrmTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collections;
import java.util.List;

@Configuration
@Import({FrameworkConfig.class, OrmTestConfiguration.class})
public class TestPropertiesServiceConfiguration {
    @Bean
    public List<String> propertiesRepositories() {
        return Collections.singletonList("src/test/resources/properties");
    }
}
