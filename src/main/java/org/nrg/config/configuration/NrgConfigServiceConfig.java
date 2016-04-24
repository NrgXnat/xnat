package org.nrg.config.configuration;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"org.nrg.config.daos", "org.nrg.config.services.impl", "org.nrg.prefs.services.impl.hibernate"})
public class NrgConfigServiceConfig {
    @Bean
    public HibernateEntityPackageList nrgConfigEntityPackages() {
        return new HibernateEntityPackageList("org.nrg.config.entities");
    }
}
