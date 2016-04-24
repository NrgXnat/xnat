package org.nrg.dicomtools.configuration;

import org.nrg.config.services.ConfigService;
import org.nrg.config.services.impl.DefaultConfigService;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.test.OrmTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan("org.nrg.config.daos")
@Import(OrmTestConfiguration.class)
public class SeriesImportFilterTestsConfiguration {
    @Bean
    public ConfigService configService() {
        return new DefaultConfigService();
    }

    @Bean
    public HibernateEntityPackageList nrgConfigEntityPackages() {
        return new HibernateEntityPackageList("org.nrg.config.entities");
    }
}
