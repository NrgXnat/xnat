/*
 * core: org.nrg.xdat.configuration.TestElementDisplayServiceConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.configuration;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.xdat.display.transport.dao.ElementDisplayDAO;
import org.nrg.xdat.display.transport.services.ElementDisplayStorageService;
import org.nrg.xdat.display.transport.services.impl.HibernateElementDisplayService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({OrmTestConfiguration.class})
public class TestElementDisplayServiceConfig {
    @Bean
    public HibernateEntityPackageList elementDisplayEntityPackages() {
        return new HibernateEntityPackageList("org.nrg.xdat.display.transport.entities");
    }

    @Bean
    public ElementDisplayDAO elementDisplayDAO() {
        return new ElementDisplayDAO();
    }

    @Bean
    public ElementDisplayStorageService elementDisplayStorageService(final ElementDisplayDAO elementDisplayDAO) {
        return new HibernateElementDisplayService(elementDisplayDAO);
    }
}
