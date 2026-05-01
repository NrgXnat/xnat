/*
 * spawner: org.nrg.xnat.spawner.configuration.HibernateSpawnerServiceTestConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.services.NrgEventService;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.prefs.configuration.NrgPrefsConfiguration;
import org.nrg.prefs.resolvers.SimplePrefsEntityResolver;
import org.nrg.prefs.services.PreferenceService;
import org.nrg.test.utils.TestBeans;
import org.nrg.xnat.spawner.entities.SpawnerElement;
import org.nrg.xnat.spawner.services.SpawnerResourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import reactor.bus.EventBus;

import java.io.IOException;

@Configuration
@ComponentScan({"org.nrg.xnat.spawner.components", "org.nrg.xnat.spawner.preferences", "org.nrg.xnat.spawner.repositories", "org.nrg.xnat.spawner.services.impl.hibernate"})
@Import({OrmTestConfiguration.class, BasicTestConfiguration.class, NrgPrefsConfiguration.class})
public class HibernateSpawnerServiceTestConfiguration {
    @Bean
    public ConfigPaths getConfigPaths() {
        return new ConfigPaths();
    }

    @Bean
    public OrderedProperties getOrderedProperties() {
        return new OrderedProperties();
    }

    @Bean
    public HibernateEntityPackageList spawnerServiceEntities() {
        return new HibernateEntityPackageList(SpawnerElement.class.getPackage().getName());
    }

    @Bean
    public SpawnerResourceLocator spawnerResourceLocator() {
        return new SpawnerResourceLocator();
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
    public JsonNode siteMap() throws IOException {
        return TestBeans.getDefaultTestSiteMap();
    }

    @Bean
    public SimplePrefsEntityResolver defaultResolver(final PreferenceService preferenceService, final JsonNode siteMap) throws IOException {
        return new SimplePrefsEntityResolver(preferenceService, siteMap);
    }
}
