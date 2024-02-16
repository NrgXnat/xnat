package org.nrg.xnat.compute.config;

import org.nrg.xnat.compute.services.ConstraintConfigEntityService;
import org.nrg.xnat.compute.services.impl.DefaultConstraintConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({HibernateEntityServicesConfig.class})
public class DefaultConstraintConfigServiceTestConfig {

    @Bean
    public DefaultConstraintConfigService defaultConstraintConfigService(final ConstraintConfigEntityService constraintConfigEntityService) {
        return new DefaultConstraintConfigService(
                constraintConfigEntityService
        );
    }

}
