package org.nrg.xnat.configuration;

import org.apache.commons.lang3.StringUtils;
import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.config.services.SiteConfigurationService;
import org.nrg.xdat.security.services.FeatureRepositoryServiceI;
import org.nrg.xdat.security.services.FeatureServiceI;
import org.nrg.xdat.security.services.RoleRepositoryServiceI;
import org.nrg.xdat.security.services.RoleServiceI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeaturesConfig {

    @Bean
    public FeatureServiceI featureService() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String serviceImpl = null;
        try {
            serviceImpl = _configurationService.getSiteConfigurationProperty("security.services.feature.default");
        } catch (SiteConfigurationException e) {
            _log.warn("An error occurred trying to retrieve the security.services.feature.default site configuration property. Continuing and using default value.", e);
        }
        if (StringUtils.isBlank(serviceImpl)) {
            serviceImpl = StringUtils.isNotBlank(_featureServiceImpl) ? _featureServiceImpl : DEFAULT_FEATURE_SERVICE;
        }
        final Class<? extends FeatureServiceI> clazz = Class.forName(serviceImpl).asSubclass(FeatureServiceI.class);
        return clazz.newInstance();
    }

    @Bean
    public FeatureRepositoryServiceI featureRepositoryService() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String serviceImpl = null;
        try {
            serviceImpl = _configurationService.getSiteConfigurationProperty("security.services.featureRepository.default");
        } catch (SiteConfigurationException e) {
            _log.warn("An error occurred trying to retrieve the security.services.featureRepository.default site configuration property. Continuing and using default value.", e);
        }
        if (StringUtils.isBlank(serviceImpl)) {
            serviceImpl = StringUtils.isNotBlank(_featureRepoServiceImpl) ? _featureRepoServiceImpl : DEFAULT_FEATURE_REPO_SERVICE;
        }
        final Class<? extends FeatureRepositoryServiceI> clazz = Class.forName(serviceImpl).asSubclass(FeatureRepositoryServiceI.class);
        return clazz.newInstance();
    }

    @Bean
    public RoleServiceI roleService() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String serviceImpl = null;
        try {
            serviceImpl = _configurationService.getSiteConfigurationProperty("security.services.role.default");
        } catch (SiteConfigurationException e) {
            _log.warn("An error occurred trying to retrieve the security.services.role.default site configuration property. Continuing and using default value.", e);
        }
        if (StringUtils.isBlank(serviceImpl)) {
            serviceImpl = StringUtils.isNotBlank(_roleServiceImpl) ? _roleServiceImpl : DEFAULT_ROLE_SERVICE;
        }
        final Class<? extends RoleServiceI> clazz = Class.forName(serviceImpl).asSubclass(RoleServiceI.class);
        return clazz.newInstance();
    }

    @Bean
    public RoleRepositoryServiceI roleRepositoryService() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String serviceImpl = null;
        try {
            serviceImpl = _configurationService.getSiteConfigurationProperty("security.services.roleRepository.default");
        } catch (SiteConfigurationException e) {
            _log.warn("An error occurred trying to retrieve the security.services.roleRepository.default site configuration property. Continuing and using default value.", e);
        }
        if (StringUtils.isBlank(serviceImpl)) {
            serviceImpl = StringUtils.isNotBlank(_roleRepoServiceImpl) ? _roleRepoServiceImpl : DEFAULT_ROLE_REPO_SERVICE;
        }
        final Class<? extends RoleRepositoryServiceI> clazz = Class.forName(serviceImpl).asSubclass(RoleRepositoryServiceI.class);
        return clazz.newInstance();
    }

    private static final String DEFAULT_FEATURE_SERVICE = "org.nrg.xdat.security.services.impl.FeatureServiceImpl";
    private static final String DEFAULT_FEATURE_REPO_SERVICE = "org.nrg.xdat.security.services.impl.FeatureRepositoryServiceImpl";
    private static final String DEFAULT_ROLE_SERVICE = "org.nrg.xdat.security.services.impl.RoleServiceImpl";
    private static final String DEFAULT_ROLE_REPO_SERVICE = "org.nrg.xdat.security.services.impl.RoleRepositoryServiceImpl";
    private static final Logger _log = LoggerFactory.getLogger(FeaturesConfig.class);

    @Autowired
    private SiteConfigurationService _configurationService;

    @Value("${security.services.feature.default:}")
    private String _featureServiceImpl;

    @Value("${security.services.featureRepository.default:}")
    private String _featureRepoServiceImpl;

    @Value("${security.services.role.default:}")
    private String _roleServiceImpl;

    @Value("${security.services.roleRepository.default:}")
    private String _roleRepoServiceImpl;
}
