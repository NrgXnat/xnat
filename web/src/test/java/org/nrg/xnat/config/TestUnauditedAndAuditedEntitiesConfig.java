package org.nrg.xnat.config;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.test.OrmTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ComponentScan({"org.nrg.xnat.test.repositories", "org.nrg.xnat.test.services.impl.hibernate"})
@Import(OrmTestConfiguration.class)
@Slf4j
public class TestUnauditedAndAuditedEntitiesConfig {
    @Bean
    public HibernateEntityPackageList testEntityPackages() {
        return new HibernateEntityPackageList("org.nrg.xnat.test.entities");
    }
}
