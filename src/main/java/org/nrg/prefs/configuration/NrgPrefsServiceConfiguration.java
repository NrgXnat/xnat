package org.nrg.prefs.configuration;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"org.nrg.prefs.services.impl.hibernate", "org.nrg.prefs.repositories"})
public class NrgPrefsServiceConfiguration {
    @Bean
    public HibernateEntityPackageList nrgPrefsHibernateEntityPackageList() {
        return new HibernateEntityPackageList("org.nrg.prefs.entities");
    }
}
