package org.nrg.xdat.configuration;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XdatTestConfig {
    @Bean
    public HibernateEntityPackageList testXdatEntityPackages() {
        return new HibernateEntityPackageList("org.nrg.xdat.entities");
    }
}
