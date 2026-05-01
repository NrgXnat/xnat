/*
 * spawner: org.nrg.xnat.spawner.configuration.DependenciesConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2017-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.configuration;

import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.configuration.SerializerConfig;
import org.nrg.framework.services.NrgEventService;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.configuration.NrgPrefsConfiguration;
import org.nrg.xdat.security.XDATUserMgmtServiceImpl;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import reactor.bus.EventBus;

import javax.sql.DataSource;

// This is strictly to let IDEA's Spring context mapping work properly. Don't load this for ANYTHING!
@Configuration
@Import({NrgPrefsConfiguration.class, SerializerConfig.class, OrmTestConfiguration.class})
public class DependenciesConfig {
    @Bean
    public ConfigPaths getConfigPaths() {
        return new ConfigPaths();
    }

    @Bean
    public OrderedProperties getOrderedProperties() {
        return new OrderedProperties();
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(final DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public UserManagementServiceI userManagementService(final NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new XDATUserMgmtServiceImpl(namedParameterJdbcTemplate);
    }

    @Bean
    public RoleHolder roleHolder() {
        return Roles.getRoleService();
    }

    @Bean
    public EventBus eventBus() {
        return EventBus.create();
    }

    @Bean
    public NrgEventService eventService(final EventBus eventBus) {
        return new NrgEventService(eventBus);
    }
}
