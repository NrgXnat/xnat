/*
 * core: org.nrg.xft.services.XftTestConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.services;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XftTestConfig {
    @Bean
    public HibernateEntityPackageList testXftEntityPackages() {
        return new HibernateEntityPackageList("org.nrg.xft.entities");
    }
}
