package org.nrg.prefs.configuration;

import org.nrg.framework.configuration.FrameworkConfig;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.prefs.configuration.NrgPrefsServiceConfiguration;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.prefs.resolvers.SimplePrefsEntityResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.IOException;

@Configuration
@Import({OrmTestConfiguration.class, FrameworkConfig.class, NrgPrefsServiceConfiguration.class})
public class PreferenceServiceTestsConfiguration {
    @Bean
    public PreferenceEntityResolver defaultResolver() throws IOException {
        return new SimplePrefsEntityResolver();
    }
}
