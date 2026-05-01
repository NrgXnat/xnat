/*
 * framework: org.nrg.framework.services.TestMarshallerCacheServiceConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.services;

import org.nrg.framework.configuration.SerializerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.List;

@Configuration
@ComponentScan("org.nrg.framework.services.impl")
@Import(SerializerConfig.class)
public class TestMarshallerCacheServiceConfiguration {
    @Bean
    public List<String> marshalablePackages() {
        return Arrays.asList("org.nrg.framework.test.models.containers", "org.nrg.framework.test.models.entities");
    }
}
