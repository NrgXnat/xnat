/*
 * framework: org.nrg.framework.configuration.FrameworkConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.configuration;

import org.nrg.framework.datacache.SerializerRegistry;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan({"org.nrg.framework.datacache.impl.hibernate", "org.nrg.framework.services.impl"})
@Import(SerializerConfig.class)
public class FrameworkConfig {
    @Bean
    public HibernateEntityPackageList frameworkEntityPackageList() {
        return new HibernateEntityPackageList("org.nrg.framework.datacache");
    }

    @Bean
    public SerializerRegistry serializerRegistry() {
        return new SerializerRegistry();
    }
}
