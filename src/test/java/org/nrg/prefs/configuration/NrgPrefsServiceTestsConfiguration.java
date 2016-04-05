package org.nrg.prefs.configuration;

import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.prefs.resolvers.SimplePrefsEntityResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.IOException;

@Configuration
@EnableTransactionManagement
@Import({OrmTestConfiguration.class, NrgPrefsServiceConfiguration.class})
@ComponentScan("org.nrg.prefs.tools")
public class NrgPrefsServiceTestsConfiguration {
    @Bean
    public PreferenceEntityResolver defaultResolver() throws IOException {
        return new SimplePrefsEntityResolver();
    }
}
