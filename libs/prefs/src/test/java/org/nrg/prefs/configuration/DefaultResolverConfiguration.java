/*
 * prefs: org.nrg.prefs.configuration.DefaultResolverConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.prefs.resolvers.SimplePrefsEntityResolver;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.test.utils.TestBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
@Import(NrgPrefsConfiguration.class)
public class DefaultResolverConfiguration {
    @Bean
    public JsonNode siteMap() throws IOException {
        return TestBeans.getDefaultTestSiteMap();
    }

    @Bean
    public PreferenceEntityResolver defaultResolver(final PreferenceService service, final JsonNode siteMap) throws IOException {
        return new SimplePrefsEntityResolver(service, siteMap);
    }
}
