/*
 * config: org.nrg.config.configuration.NrgConfigTestConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config.configuration;

import org.nrg.config.resolvers.SimplePrefsEntityResolver;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.prefs.configuration.NrgPrefsConfiguration;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Configuration
@Import({NrgConfigConfiguration.class, OrmTestConfiguration.class, NrgPrefsConfiguration.class})
@ComponentScan("org.nrg.config.util")
public class NrgConfigTestConfiguration {
    @Bean
    public List<String> configFilesLocations() {
	    return Collections.singletonList("src/test/resources/org/nrg/config");
    }

    @Bean
    public PreferenceEntityResolver defaultResolver() throws IOException {
        return new SimplePrefsEntityResolver();
    }
}
