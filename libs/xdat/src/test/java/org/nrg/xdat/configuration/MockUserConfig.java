/*
 * core: org.nrg.xdat.configuration.MockUserConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.configuration;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.xdat.configuration.mocks.MockUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.nrg.xdat.configuration.mocks")
public class MockUserConfig {
    @Bean
    public HibernateEntityPackageList mockUserEntities() {
        return new HibernateEntityPackageList(MockUser.class.getPackage().getName());
    }
}
