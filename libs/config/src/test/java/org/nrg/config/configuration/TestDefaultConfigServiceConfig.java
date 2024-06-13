package org.nrg.config.configuration;

import org.nrg.config.daos.ConfigurationDAO;
import org.nrg.config.daos.ConfigurationDataDAO;
import org.nrg.config.services.ConfigService;
import org.nrg.config.services.impl.DefaultConfigService;
import org.nrg.framework.test.OrmTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ComponentScan({"org.nrg.config.daos", "org.nrg.prefs.repositories"})
@Import(OrmTestConfiguration.class)
public class TestDefaultConfigServiceConfig {
    @Bean
    public ConfigService configService(final ConfigurationDAO configurationDAO, final ConfigurationDataDAO dataDAO, final PlatformTransactionManager transactionManager, final JdbcTemplate template) {
        return new DefaultConfigService(configurationDAO, dataDAO, transactionManager, template);
    }
}
