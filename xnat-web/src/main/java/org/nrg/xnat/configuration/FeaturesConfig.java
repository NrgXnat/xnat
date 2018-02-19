/*
 * web: org.nrg.xnat.configuration.FeaturesConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.services.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.nrg.xdat.security.services.FeatureRepositoryServiceI.DEFAULT_FEATURE_REPO_SERVICE;
import static org.nrg.xdat.security.services.FeatureServiceI.DEFAULT_FEATURE_SERVICE;
import static org.nrg.xdat.security.services.RoleRepositoryServiceI.DEFAULT_ROLE_REPO_SERVICE;
import static org.nrg.xdat.security.services.RoleServiceI.DEFAULT_ROLE_SERVICE;

@Configuration
@Slf4j
public class FeaturesConfig {

    @Bean
    public FeatureServiceI featureService(final SiteConfigPreferences preferences) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        final String serviceImpl = StringUtils.defaultIfBlank(preferences.getFeatureService(), DEFAULT_FEATURE_SERVICE);
        log.debug("Creating feature service with implementing class {}", serviceImpl);
        return Class.forName(serviceImpl).asSubclass(FeatureServiceI.class).newInstance();
    }

    @Bean
    public FeatureRepositoryServiceI featureRepositoryService(final SiteConfigPreferences preferences) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        final String serviceImpl = StringUtils.defaultIfBlank(preferences.getFeatureRepositoryService(), DEFAULT_FEATURE_REPO_SERVICE);
        log.debug("Creating feature repository service with implementing class {}", serviceImpl);
        return Class.forName(serviceImpl).asSubclass(FeatureRepositoryServiceI.class).newInstance();
    }

    @Bean
    public RoleHolder roleService(final SiteConfigPreferences preferences) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        final String serviceImpl = StringUtils.defaultIfBlank(preferences.getRoleService(), DEFAULT_ROLE_SERVICE);
        log.debug("Creating role service with implementing class {}", serviceImpl);
        return new RoleHolder(Class.forName(serviceImpl).asSubclass(RoleServiceI.class).newInstance());
    }

    @Bean
    public RoleRepositoryHolder roleRepositoryService(final SiteConfigPreferences preferences) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        final String serviceImpl = StringUtils.defaultIfBlank(preferences.getRoleRepositoryService(), DEFAULT_ROLE_REPO_SERVICE);
        log.debug("Creating role repository service with implementing class {}", serviceImpl);
        return new RoleRepositoryHolder(Class.forName(serviceImpl).asSubclass(RoleRepositoryServiceI.class).newInstance());
    }
}
