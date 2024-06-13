/*
 * dicomtools: org.nrg.dicomtools.configuration.SeriesImportFilterTestsConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicomtools.configuration;

import org.nrg.config.daos.ConfigurationDAO;
import org.nrg.config.daos.ConfigurationDataDAO;
import org.nrg.config.services.ConfigService;
import org.nrg.config.services.impl.DefaultConfigService;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.test.OrmTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ComponentScan({"org.nrg.config.daos", "org.nrg.dicomtools.filters"})
@Import({OrmTestConfiguration.class})
@ImportResource("classpath:/META-INF/configuration/nrg-automation-context.xml")
@PropertySources(@PropertySource("classpath:org/nrg/dicomtools/filters/filter-definitions.properties"))
public class SeriesImportFilterTestsConfiguration {
    @Autowired
    ConfigurationDAO dao;
    @Autowired
    ConfigurationDataDAO dataDAO;
    @Autowired
    PlatformTransactionManager transactionManager;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Bean
    public ConfigService configService() {
        return new DefaultConfigService( dao, dataDAO, transactionManager, jdbcTemplate);
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
