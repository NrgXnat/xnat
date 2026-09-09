package org.nrg.xnat.config;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.test.OrmTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@Import(OrmTestConfiguration.class)
@ComponentScan("org.nrg.xnat.customforms.daos")
public class TestCustomVariableFormAppliesToRepositoryConfig {
    @Bean
    public HibernateEntityPackageList customFormsEntityPackages() {
        return new HibernateEntityPackageList("org.nrg.xnat.entities");
    }
}
