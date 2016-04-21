package org.nrg.xnat.configuration;

import org.apache.commons.lang3.StringUtils;
import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.xdat.security.services.FeatureRepositoryServiceI;
import org.nrg.xdat.security.services.FeatureServiceI;
import org.nrg.xdat.security.services.RoleRepositoryServiceI;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xnat.initialization.InitializerSiteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

@Configuration
public class FeaturesConfig {

    public static final String DEFAULT_FEATURE_SERVICE = "org.nrg.xdat.security.services.impl.FeatureServiceImpl";
    public static final String DEFAULT_FEATURE_REPO_SERVICE = "org.nrg.xdat.security.services.impl.FeatureRepositoryServiceImpl";
    public static final String DEFAULT_ROLE_SERVICE = "org.nrg.xdat.security.services.impl.RoleServiceImpl";
    public static final String DEFAULT_ROLE_REPO_SERVICE = "org.nrg.xdat.security.services.impl.RoleRepositoryServiceImpl";

    @Bean
    public FeatureServiceI featureService() throws ClassNotFoundException, IllegalAccessException, InstantiationException, SiteConfigurationException {
        final String serviceImpl = StringUtils.defaultIfBlank(_preferences.getFeatureService(), DEFAULT_FEATURE_SERVICE);
        if (_log.isDebugEnabled()) {
            _log.debug("Creating feature service with implementing class " + serviceImpl);
        }
        return Class.forName(serviceImpl).asSubclass(FeatureServiceI.class).newInstance();
    }

    @Bean
    public FeatureRepositoryServiceI featureRepositoryService() throws ClassNotFoundException, IllegalAccessException, InstantiationException, SiteConfigurationException {
        final String serviceImpl = StringUtils.defaultIfBlank(_preferences.getFeatureRepositoryService(), DEFAULT_FEATURE_REPO_SERVICE);
        if (_log.isDebugEnabled()) {
            _log.debug("Creating feature repository service with implementing class " + serviceImpl);
        }
        return Class.forName(serviceImpl).asSubclass(FeatureRepositoryServiceI.class).newInstance();
    }

    @Bean
    public RoleServiceI roleService() throws ClassNotFoundException, IllegalAccessException, InstantiationException, SiteConfigurationException {
        final String serviceImpl = StringUtils.defaultIfBlank(_preferences.getRoleService(), DEFAULT_ROLE_SERVICE);
        if (_log.isDebugEnabled()) {
            _log.debug("Creating role service with implementing class " + serviceImpl);
        }
        return Class.forName(serviceImpl).asSubclass(RoleServiceI.class).newInstance();
    }

    @Bean
    public RoleRepositoryServiceI roleRepositoryService() throws ClassNotFoundException, IllegalAccessException, InstantiationException, SiteConfigurationException {
        final String serviceImpl = StringUtils.defaultIfBlank(_preferences.getRoleRepositoryService(), DEFAULT_ROLE_REPO_SERVICE);
        if (_log.isDebugEnabled()) {
            _log.debug("Creating role repository service with implementing class " + serviceImpl);
        }
        return Class.forName(serviceImpl).asSubclass(RoleRepositoryServiceI.class).newInstance();
    }

    private static final Logger _log = LoggerFactory.getLogger(FeaturesConfig.class);

    @Inject
    private InitializerSiteConfiguration _preferences;
}
