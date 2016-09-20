/*
 * org.nrg.automation.configuration.AutomationTestsConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.configuration;

import org.nrg.automation.services.ScriptRunnerService;
import org.nrg.automation.services.ScriptService;
import org.nrg.automation.services.ScriptTriggerService;
import org.nrg.automation.services.impl.DefaultScriptRunnerService;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.test.OrmTestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Collections;

@Configuration
@ComponentScan({"org.nrg.automation.services.impl.hibernate", "org.nrg.automation.repositories", "org.nrg.automation.daos"})
@Import(OrmTestConfiguration.class)
public class AutomationTestsConfiguration {
    @Bean
    public ScriptRunnerService scriptRunnerService(final ScriptService scriptService, final ScriptTriggerService triggerService) {
        return new DefaultScriptRunnerService(scriptService, triggerService) {{
            setRunnerPackages(Collections.singletonList("org.nrg.automation.runners"));
        }};
    }

    @Bean
    public HibernateEntityPackageList nrgAutomationEntityPackages() {
        return new HibernateEntityPackageList("org.nrg.automation.entities");
    }
}
