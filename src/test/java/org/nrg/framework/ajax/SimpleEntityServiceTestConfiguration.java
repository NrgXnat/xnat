/*
 * framework: org.nrg.framework.ajax.SimpleEntityServiceTestConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.ajax;

import org.nrg.framework.configuration.SerializerConfig;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.test.OrmTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({OrmTestConfiguration.class, SerializerConfig.class})
@ComponentScan({"org.nrg.framework.ajax", "org.nrg.framework.orm.utils"})
public class SimpleEntityServiceTestConfiguration {
    @Bean
    public HibernateEntityPackageList pacsEntitiesPackageList() {
        return new HibernateEntityPackageList("org.nrg.framework.ajax");
    }
}
