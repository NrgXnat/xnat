/*
 * core: org.nrg.xdat.configuration.TestGroupFeatureServiceConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.configuration;

import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.xdat.daos.GroupFeatureDAO;
import org.nrg.xdat.services.GroupFeatureService;
import org.nrg.xdat.services.impl.hibernate.HibernateGroupFeatureService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({OrmTestConfiguration.class, XdatTestConfig.class})
public class TestGroupFeatureServiceConfig {
    @Bean
    public GroupFeatureService groupFeatureService() {
        return new HibernateGroupFeatureService();
    }

    @Bean
    public GroupFeatureDAO groupFeatureDAO() {
        return new GroupFeatureDAO();
    }
}
