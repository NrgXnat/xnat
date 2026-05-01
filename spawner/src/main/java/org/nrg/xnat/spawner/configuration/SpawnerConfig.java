/*
 * spawner: org.nrg.xnat.spawner.configuration.SpawnerConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.configuration;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.xnat.spawner.services.SpawnerResourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@ComponentScan({"org.nrg.xnat.spawner.components",
                "org.nrg.xnat.spawner.preferences",
                "org.nrg.xnat.spawner.repositories",
                "org.nrg.xnat.spawner.rest",
                "org.nrg.xnat.spawner.services.impl.hibernate",
                "org.nrg.xnat.spawner.validation"})
public class SpawnerConfig {
    @Bean
    public HibernateEntityPackageList spawnerEntityPackages() {
        return new HibernateEntityPackageList(Collections.singletonList("org.nrg.xnat.spawner.entities"));
    }

    @Bean
    public SpawnerResourceLocator spawnerResourceLocator() {
        // TODO: This uses the default spawner element pattern. It would be nice to set this as a site configuration property.
        return new SpawnerResourceLocator();
    }
}
