package org.nrg.xnat.configuration;

import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.config.services.ConfigService;
import org.nrg.config.services.SiteConfigurationService;
import org.nrg.config.services.UserConfigurationService;
import org.nrg.config.services.impl.DefaultConfigService;
import org.nrg.config.services.impl.DefaultUserConfigurationService;
import org.nrg.prefs.configuration.NrgPrefsConfiguration;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.resolvers.XnatPreferenceEntityResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Configuration
@ComponentScan("org.nrg.config.daos")
@Import(NrgPrefsConfiguration.class)
public class PreferencesConfig {

    @Bean
    public ConfigService configService() {
        return new DefaultConfigService();
    }

    @Bean
    public UserConfigurationService userConfigurationService() {
        return new DefaultUserConfigurationService(configService());
    }

    @Bean
    public SiteConfigurationService siteConfigurationService(final SiteConfigPreferences preferences, final NrgPreferenceService nrgPreferenceService, final ConfigService configService, final Environment environment) throws ClassNotFoundException, IllegalAccessException, InstantiationException, SiteConfigurationException {
        final Class<? extends SiteConfigurationService> clazz = Class.forName(preferences.getSiteConfigurationService()).asSubclass(SiteConfigurationService.class);
        try {
            @SuppressWarnings("unchecked")
            final Constructor<? extends SiteConfigurationService> constructor = clazz.getConstructor(NrgPreferenceService.class, Environment.class);
            return constructor.newInstance(nrgPreferenceService, environment);
        } catch (NoSuchMethodException | InvocationTargetException ignored) {
            // No worries, it just doesn't have that constructor.
        }
        try {
            @SuppressWarnings("unchecked")
            final Constructor<? extends SiteConfigurationService> constructor = clazz.getConstructor(ConfigService.class, Environment.class);
            return constructor.newInstance(configService, environment);
        } catch (NoSuchMethodException | InvocationTargetException ignored) {
            // No worries, it just doesn't have that constructor.
        }
        return clazz.newInstance();
    }

    @Bean
    public XnatPreferenceEntityResolver defaultResolver(final PreferenceService preferenceService) throws IOException {
        return new XnatPreferenceEntityResolver(preferenceService);
    }
}
