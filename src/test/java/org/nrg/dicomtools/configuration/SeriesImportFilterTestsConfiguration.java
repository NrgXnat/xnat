package org.nrg.dicomtools.configuration;

import org.nrg.config.services.ConfigService;
import org.nrg.config.services.impl.DefaultConfigService;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.test.OrmTestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan({"org.nrg.config.daos", "org.nrg.dicomtools.filters"})
@Import({OrmTestConfiguration.class})
@ImportResource("classpath:/META-INF/configuration/nrg-automation-context.xml")
@PropertySource("classpath:org/nrg/dicomtools/filters/filter-definitions.properties")
public class SeriesImportFilterTestsConfiguration {
    @Bean
    public ConfigService configService() {
        return new DefaultConfigService();
    }

    @Bean
    public HibernateEntityPackageList nrgConfigEntityPackages() {
        return new HibernateEntityPackageList("org.nrg.config.entities");
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
