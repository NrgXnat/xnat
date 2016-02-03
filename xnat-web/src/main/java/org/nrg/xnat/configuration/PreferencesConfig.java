package org.nrg.xnat.configuration;

import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.config.services.ConfigService;
import org.nrg.config.services.SiteConfigurationService;
import org.nrg.config.services.UserConfigurationService;
import org.nrg.config.services.impl.DefaultConfigService;
import org.nrg.config.services.impl.DefaultUserConfigurationService;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.xnat.resolvers.XnatPreferenceEntityResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Collections;

@Configuration
@ComponentScan({"org.nrg.config.daos", "org.nrg.prefs.services.impl.hibernate", "org.nrg.prefs.repositories"})
public class PreferencesConfig {

    @Bean
    public ConfigService configService() {
        return new DefaultConfigService();
    }

    @Bean
    public UserConfigurationService userConfigurationService() {
        return new DefaultUserConfigurationService();
    }

    @Bean
    public SiteConfigurationService siteConfigurationService() throws ClassNotFoundException, IllegalAccessException, InstantiationException, SiteConfigurationException {
        // TODO: Add direct prefs call or JDBC call to get the site configuration setting from the database.
        final Class<? extends SiteConfigurationService> clazz = Class.forName(_siteConfigServiceImpl).asSubclass(SiteConfigurationService.class);
        return clazz.newInstance();
    }

    @Bean
    public HibernateEntityPackageList nrgPrefsEntityPackages() {
        return new HibernateEntityPackageList(Collections.singletonList("org.nrg.prefs.entities"));
    }

    @Bean
    public XnatPreferenceEntityResolver defaultResolver(final PreferenceService preferenceService) throws IOException {
        return new XnatPreferenceEntityResolver(preferenceService);
    }

    @Value("${admin.siteConfig.service:org.nrg.config.services.impl.PrefsBasedSiteConfigurationService}")
    private String _siteConfigServiceImpl;
}
