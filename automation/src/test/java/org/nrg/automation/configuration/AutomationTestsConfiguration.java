/*
 * automation: org.nrg.automation.configuration.AutomationTestsConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.configuration;

import org.nrg.automation.services.AutomationEventIdsIdsService;
import org.nrg.automation.services.AutomationEventIdsService;
import org.nrg.automation.services.AutomationFiltersService;
import org.nrg.automation.services.AutomationService;
import org.nrg.automation.services.ScriptRunnerService;
import org.nrg.automation.services.ScriptService;
import org.nrg.automation.services.ScriptTriggerService;
import org.nrg.automation.services.impl.AutomationServiceImpl;
import org.nrg.automation.services.impl.DefaultScriptRunnerService;
import org.nrg.framework.configuration.SerializerConfig;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.test.OrmTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collections;

@Configuration
@ComponentScan({"org.nrg.automation.services.impl.hibernate", "org.nrg.automation.repositories", "org.nrg.automation.daos"})
@Import({SerializerConfig.class, OrmTestConfiguration.class})
public class AutomationTestsConfiguration {
    static {
        System.setProperty("automation.enabled", "true");
    }

    @Bean
    public ScriptRunnerService scriptRunnerService(final AutomationService automationService, final ScriptService scriptService, final ScriptTriggerService triggerService) {
        DefaultScriptRunnerService service = new DefaultScriptRunnerService(automationService, scriptService, triggerService);
        service.setRunnerPackages(Collections.singletonList("org.nrg.automation.runners"));
        return service;
    }

    @Bean
    public HibernateEntityPackageList nrgAutomationEntityPackages() {
        return new HibernateEntityPackageList("org.nrg.automation.entities", "org.nrg.automation.event.entities", "org.nrg.framework.event.entities");
    }

    @Bean
    public AutomationService automationService(final AutomationEventIdsService eventIdsService, final AutomationEventIdsIdsService eventIdsIdsService, final AutomationFiltersService filtersService) {
        return new AutomationServiceImpl(eventIdsService, eventIdsIdsService, filtersService);
    }
}
