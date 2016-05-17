package org.nrg.automation.configuration;

import org.nrg.automation.services.ScriptRunnerService;
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
    public ScriptRunnerService scriptRunnerService() {
        return new DefaultScriptRunnerService() {{
            setRunnerPackages(Collections.singletonList("org.nrg.automation.runners"));
        }};
    }

    @Bean
    public HibernateEntityPackageList nrgAutomationEntityPackages() {
        return new HibernateEntityPackageList("org.nrg.automation.entities");
    }
}
