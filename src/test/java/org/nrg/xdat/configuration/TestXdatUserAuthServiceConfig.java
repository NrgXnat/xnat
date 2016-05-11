package org.nrg.xdat.configuration;

import org.nrg.framework.configuration.FrameworkConfig;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.prefs.configuration.NrgPrefsServiceConfiguration;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.xdat.daos.XdatUserAuthDAO;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.resolvers.SimplePrefsEntityResolver;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.services.impl.hibernate.HibernateXdatUserAuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.IOException;

@Configuration
@Import({OrmTestConfiguration.class, XdatTestConfig.class, NrgPrefsServiceConfiguration.class, FrameworkConfig.class})
public class TestXdatUserAuthServiceConfig {
    @Bean
    public XdatUserAuthService xdatUserAuthService() {
        return new HibernateXdatUserAuthService();
    }

    @Bean
    public XdatUserAuthDAO xdatUserAuthDAO() {
        return new XdatUserAuthDAO();
    }

    @Bean
    public HibernateEntityPackageList dataCacheEntities() {
        return new HibernateEntityPackageList("org.nrg.xdat.entities");
    }

    @Bean
    public PreferenceEntityResolver defaultResolver() throws IOException {
        return new SimplePrefsEntityResolver();
    }

    @Bean
    public SiteConfigPreferences siteConfigPreferences() {
        return new SiteConfigPreferences();
    }
}
