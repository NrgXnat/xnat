/*
 * framework: org.nrg.framework.orm.auditable.AuditableEntityTestsConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.auditable;

import org.nrg.framework.configuration.FrameworkConfig;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.test.OrmTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
@Configuration
@Import({OrmTestConfiguration.class, FrameworkConfig.class})
@ComponentScan({"org.nrg.framework.orm.auditable", "org.nrg.framework.orm.utils"})
public class AuditableEntityTestsConfiguration {

    @Bean
    public HibernateEntityPackageList auditableEntitiesPackageList() {
        return new HibernateEntityPackageList("org.nrg.framework.orm.auditable");
    }
}
