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
