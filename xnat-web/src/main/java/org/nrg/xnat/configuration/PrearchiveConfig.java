package org.nrg.xnat.configuration;

import org.nrg.xnat.helpers.prearchive.PrearcConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PrearchiveConfig {
    @Bean
    public PrearcConfig prearcConfig() {
        final PrearcConfig prearcConfig = new PrearcConfig();
        prearcConfig.setReloadPrearcDatabaseOnApplicationStartup(false);
        return prearcConfig;
    }
}
