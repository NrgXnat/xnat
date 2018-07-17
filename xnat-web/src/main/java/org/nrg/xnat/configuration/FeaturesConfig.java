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
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.services.*;
import org.nrg.xdat.services.UserRoleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.nrg.framework.exceptions.NrgServiceError.ConfigurationError;
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
    public RoleServiceI roleService(final SiteConfigPreferences preferences, final UserRoleService service, final NamedParameterJdbcTemplate template) {
        final String serviceImpl = StringUtils.defaultIfBlank(preferences.getRoleService(), DEFAULT_ROLE_SERVICE);
        log.debug("Creating role service with implementing class {}", serviceImpl);
        final RoleServiceI roleService = Reflection.constructObjectFromParameters(serviceImpl, RoleServiceI.class, service, template);
        if (roleService != null) {
            return roleService;
        }
        final RoleServiceI roleServiceDefCon = Reflection.constructObjectFromParameters(serviceImpl, RoleServiceI.class);
        if (roleServiceDefCon == null) {
            throw new NrgServiceRuntimeException(ConfigurationError, "An error occurred trying to instantiate an instance of the {} class for the role service. Check the logs for errors.");
        }
        return roleServiceDefCon;
    }

    @Bean
    public RoleHolder roleHolder(final RoleServiceI roleService, final NamedParameterJdbcTemplate template) {
        return new RoleHolder(roleService, template);
    }

    @Bean
    public RoleRepositoryHolder roleRepositoryService(final SiteConfigPreferences preferences) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        final String serviceImpl = StringUtils.defaultIfBlank(preferences.getRoleRepositoryService(), DEFAULT_ROLE_REPO_SERVICE);
        log.debug("Creating role repository service with implementing class {}", serviceImpl);
        return new RoleRepositoryHolder(Class.forName(serviceImpl).asSubclass(RoleRepositoryServiceI.class).newInstance());
    }
}
