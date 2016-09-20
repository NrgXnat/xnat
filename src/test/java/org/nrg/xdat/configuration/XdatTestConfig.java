/*
 * core: org.nrg.xdat.configuration.XdatTestConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.configuration;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XdatTestConfig {
    @Bean
    public HibernateEntityPackageList testXdatEntityPackages() {
        return new HibernateEntityPackageList("org.nrg.xdat.entities");
    }
}
