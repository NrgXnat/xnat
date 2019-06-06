/*
 * web: org.nrg.xnat.configuration.XnatServicesConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xdat.security.PermissionsServiceImpl;
import org.nrg.xdat.security.UserGroupManager;
import org.nrg.xdat.security.UserGroupServiceI;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.services.DataTypeAwareEventService;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.IOException;

/**
 * This configuration manages configuration and instantiation of core XNAT/XDAT/XFT services.
 */
@Configuration
@ComponentScan({"org.nrg.xnat.services.archive.impl",
                "org.nrg.xnat.services.cache",
                "org.nrg.xnat.services.investigators.impl.xft",
                "org.nrg.xnat.services.system.impl.hibernate",
                "org.nrg.xnat.services.validation"})
public class XnatServicesConfig {
    @Bean
    public PermissionsServiceI permissionsService(final DataTypeAwareEventService eventService, final NamedParameterJdbcTemplate template) {
        return new PermissionsServiceImpl(eventService, template);
    }

    @Bean
    public UserGroupServiceI userGroupManager(final GroupsAndPermissionsCache cache, final DataTypeAwareEventService eventService, final NamedParameterJdbcTemplate template, final DatabaseHelper helper) throws IOException {
        return new UserGroupManager(cache, template, eventService, helper);
    }
}
