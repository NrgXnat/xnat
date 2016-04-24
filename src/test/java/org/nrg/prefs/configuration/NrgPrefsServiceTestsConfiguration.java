package org.nrg.prefs.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@Import(PreferenceServiceTestsConfiguration.class)
@ComponentScan("org.nrg.prefs.tools")
public class NrgPrefsServiceTestsConfiguration {
}
