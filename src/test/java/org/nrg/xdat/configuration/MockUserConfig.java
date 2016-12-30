package org.nrg.xdat.configuration;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.xdat.configuration.mocks.MockUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("org.nrg.xdat.configuration.mocks")
public class MockUserConfig {
    @Bean
    public HibernateEntityPackageList mockUserEntities() {
        return new HibernateEntityPackageList(MockUser.class.getPackage().getName());
    }
}
