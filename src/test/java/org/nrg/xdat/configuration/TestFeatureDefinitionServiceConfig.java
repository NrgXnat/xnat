package org.nrg.xdat.configuration;

import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.xdat.daos.FeatureDefinitionDAO;
import org.nrg.xdat.services.FeatureDefinitionService;
import org.nrg.xdat.services.impl.hibernate.HibernateFeatureDefinitionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({OrmTestConfiguration.class, XdatTestConfig.class})
public class TestFeatureDefinitionServiceConfig {
    @Bean
    public FeatureDefinitionService featureDefinitionService() {
        return new HibernateFeatureDefinitionService();
    }

    @Bean
    public FeatureDefinitionDAO featureDefinitionDAO() {
        return new FeatureDefinitionDAO();
    }
}
