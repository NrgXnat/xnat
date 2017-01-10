/*
 * web: org.nrg.xnat.configuration.PreferencesConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import org.nrg.config.services.ConfigService;
import org.nrg.config.services.UserConfigurationService;
import org.nrg.config.services.impl.DefaultConfigService;
import org.nrg.config.services.impl.DefaultUserConfigurationService;
import org.nrg.prefs.configuration.NrgPrefsConfiguration;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.xnat.resolvers.XnatPreferenceEntityResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.IOException;

@Configuration
@ComponentScan("org.nrg.config.daos")
@Import(NrgPrefsConfiguration.class)
public class PreferencesConfig {
    @Bean
    public ConfigService configService() {
        return new DefaultConfigService();
    }

    @Bean
    public UserConfigurationService userConfigurationService() {
        return new DefaultUserConfigurationService(configService());
    }

    @Bean
    public XnatPreferenceEntityResolver defaultResolver(final PreferenceService preferenceService) throws IOException {
        return new XnatPreferenceEntityResolver(preferenceService);
    }
}
