/*
 * core: org.nrg.xdat.configuration.TestFeatureDefinitionServiceConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

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
