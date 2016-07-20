package org.nrg.config.configuration;

import org.nrg.prefs.configuration.NrgPrefsConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(NrgPrefsConfiguration.class)
@ComponentScan({"org.nrg.config.daos", "org.nrg.config.services.impl", "org.nrg.prefs.services.impl.hibernate"})
public class NrgConfigConfiguration {
}
