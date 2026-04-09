package org.nrg.xnat.config;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.test.OrmTestConfiguration;
import org.nrg.xnat.hibernate.TestEventTracker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(OrmTestConfiguration.class)
@ComponentScan({"org.nrg.xnat.hibernate.repositories", "org.nrg.xnat.hibernate.services.impl.hibernate", "org.nrg.xnat.hibernate.listeners"})
public class TestHibernateEntityEventListenersConfig {
    @Bean
    public HibernateEntityPackageList hibernateEntityEventListenerEntityPackages() {
        return new HibernateEntityPackageList("org.nrg.xnat.hibernate.entities");
    }

    @Bean
    public TestEventTracker testEventTracker() {
        return new TestEventTracker();
    }
}
