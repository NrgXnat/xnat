/*
 * core: org.nrg.xdat.configuration.TestXdatUserAuthServiceConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import org.nrg.framework.configuration.FrameworkConfig;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.services.NrgEventService;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.prefs.configuration.NrgPrefsConfiguration;
import org.nrg.prefs.resolvers.PreferenceEntityResolver;
import org.nrg.prefs.resolvers.SimplePrefsEntityResolver;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.test.utils.TestBeans;
import org.nrg.xdat.daos.XdatUserAuthDAO;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.services.impl.hibernate.HibernateXdatUserAuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import reactor.bus.EventBus;

import java.io.IOException;

@Configuration
@Import({OrmTestConfiguration.class, XdatTestConfig.class, NrgPrefsConfiguration.class, FrameworkConfig.class, MockUserConfig.class})
public class TestXdatUserAuthServiceConfig {
    @Bean
    public XdatUserAuthService xdatUserAuthService() {
        return new HibernateXdatUserAuthService();
    }

    @Bean
    public XdatUserAuthDAO xdatUserAuthDAO() {
        return new XdatUserAuthDAO();
    }

    @Bean
    public HibernateEntityPackageList dataCacheEntities() {
        return new HibernateEntityPackageList("org.nrg.xdat.entities");
    }

    @Bean
    public JsonNode siteMap() throws IOException {
        return TestBeans.getDefaultTestSiteMap();
    }

    @Bean
    public PreferenceEntityResolver defaultResolver(final PreferenceService service, final JsonNode siteMap) throws IOException {
        return new SimplePrefsEntityResolver(service, siteMap);
    }

    @Bean
    public EventBus eventBus() {
        return EventBus.create();
    }

    @Bean
    public NrgEventService eventService(final EventBus eventBus) {
        return new NrgEventService(eventBus);
    }

    @Bean
    public SiteConfigPreferences siteConfigPreferences(final NrgPreferenceService preferenceService, final NrgEventService eventService) {
        return new SiteConfigPreferences(preferenceService, eventService, null, null);
    }
}
