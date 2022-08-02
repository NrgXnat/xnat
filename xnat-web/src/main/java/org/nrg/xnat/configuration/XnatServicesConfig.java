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
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.PermissionsServiceImpl;
import org.nrg.xdat.security.UserGroupManager;
import org.nrg.xdat.security.UserGroupServiceI;
import org.nrg.xdat.security.services.PermissionsServiceI;
import org.nrg.xdat.security.services.ScanSecurityService;
import org.nrg.xdat.services.DataTypeAwareEventService;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.identifier.IDGeneratorI;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.services.system.HostInfoService;
import org.nrg.xnat.turbine.utils.IDGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.nrg.xft.identifier.IDGeneratorFactory.*;

/**
 * This configuration manages configuration and instantiation of core XNAT/XDAT/XFT services.
 */
@Configuration
@ComponentScan({"org.nrg.xft.identifier", "org.nrg.xnat.eventservice", "org.nrg.xnat.services.archive.impl",
                "org.nrg.xnat.services.cache", "org.nrg.xnat.services.investigators.impl.xft",
                "org.nrg.xnat.services.system.impl.hibernate", "org.nrg.xnat.services.triage",
                "org.nrg.xnat.services.validation", "org.nrg.xnat.snapshot.generator.impl",
                "org.nrg.xnat.snapshot.services.impl", "org.nrg.xnat.services.security",})
public class XnatServicesConfig {
    @Bean
    public PermissionsServiceI permissionsService(final DataTypeAwareEventService eventService,
                                                  final NamedParameterJdbcTemplate template,
                                                  final ScanSecurityService scanSecurityService) {
        return new PermissionsServiceImpl(eventService, template, scanSecurityService);
    }

    @Bean
    public UserGroupServiceI userGroupManager(final GroupsAndPermissionsCache cache, final DataTypeAwareEventService eventService, final NamedParameterJdbcTemplate template, final DatabaseHelper helper) {
        return new UserGroupManager(cache, template, eventService, helper);
    }

    @Bean
    public IDGeneratorI subjectIdGenerator(final JdbcTemplate template, final SiteConfigPreferences preferences, final HostInfoService hostInfoService, final XnatAppInfo appInfo) {
        final IDGeneratorI generator = new IDGenerator(template, preferences, hostInfoService, appInfo);
        generator.setTable(KEY_SUBJECTS);
        generator.setDigits(DEFAULT_DIGITS);
        generator.setColumn(DEFAULT_COLUMN);
        return generator;
    }

    @Bean
    public IDGeneratorI experimentIdGenerator(final JdbcTemplate template, final SiteConfigPreferences preferences, final HostInfoService hostInfoService, final XnatAppInfo appInfo) {
        final IDGeneratorI generator = new IDGenerator(template, preferences, hostInfoService, appInfo);
        generator.setTable(KEY_EXPERIMENTS);
        generator.setDigits(DEFAULT_DIGITS);
        generator.setColumn(DEFAULT_COLUMN);
        return generator;
    }

    @Bean
    public IDGeneratorI visitIdGenerator(final JdbcTemplate template, final SiteConfigPreferences preferences, final HostInfoService hostInfoService, final XnatAppInfo appInfo) {
        final IDGeneratorI generator = new IDGenerator(template, preferences, hostInfoService, appInfo);
        generator.setTable(KEY_VISITS);
        generator.setDigits(DEFAULT_DIGITS);
        generator.setColumn(DEFAULT_COLUMN);
        return generator;
    }
}
