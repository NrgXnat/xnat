package org.nrg.xnat.configuration;

import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.config.services.ConfigService;
import org.nrg.config.services.SiteConfigurationService;
import org.nrg.config.services.UserConfigurationService;
import org.nrg.config.services.impl.DefaultConfigService;
import org.nrg.config.services.impl.DefaultUserConfigurationService;
import org.nrg.prefs.configuration.NrgPrefsServiceConfiguration;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.xdat.preferences.InitializerSiteConfiguration;
import org.nrg.xnat.resolvers.XnatPreferenceEntityResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;
import java.io.IOException;

@Configuration
@ComponentScan("org.nrg.config.daos")
@Import(NrgPrefsServiceConfiguration.class)
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
        final Class<? extends SiteConfigurationService> clazz = Class.forName(_preferences.getSiteConfigurationService()).asSubclass(SiteConfigurationService.class);
        return clazz.newInstance();
    }

    @Bean
    public XnatPreferenceEntityResolver defaultResolver(final PreferenceService preferenceService) throws IOException {
        return new XnatPreferenceEntityResolver(preferenceService);
    }

    @Inject
    private InitializerSiteConfiguration _preferences;
}
