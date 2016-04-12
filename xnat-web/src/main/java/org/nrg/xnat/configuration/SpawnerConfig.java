package org.nrg.xnat.configuration;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.xnat.spawner.services.SpawnerResourceLocator;
import org.nrg.xnat.spawner.services.impl.SpawnerWorker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@ComponentScan({"org.nrg.xnat.spawner.controllers", "org.nrg.xnat.spawner.services.impl.hibernate", "org.nrg.xnat.spawner.repositories"})
public class SpawnerConfig {
    @Bean
    public SpawnerWorker spawnerWorker() {
        return new SpawnerWorker();
    }

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
