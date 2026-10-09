package org.nrg.xnat.archive.services.impl;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.test.OrmTestConfiguration;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.archive.daos.DirectArchiveSessionDao;
import org.nrg.xnat.archive.services.DirectArchiveSessionHibernateService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.mock;

/**
 * Wires the direct archive session entity, DAO and Hibernate service against the shared ORM test database so the
 * status transitions can be exercised with real transactions.
 */
@Configuration
@Import(OrmTestConfiguration.class)
public class DirectArchiveSessionOrmTestConfig {
    @Bean
    public HibernateEntityPackageList directArchiveEntities() {
        return new HibernateEntityPackageList("org.nrg.xnat.archive.entities");
    }

    @Bean
    public DirectArchiveSessionDao directArchiveSessionDao() {
        return new DirectArchiveSessionDao(mock(SiteConfigPreferences.class));
    }

    @Bean
    public DirectArchiveSessionHibernateService directArchiveSessionHibernateService() {
        return new DirectArchiveSessionHibernateServiceImpl();
    }
}
