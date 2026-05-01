/*
 * prefs: org.nrg.prefs.configuration.AliasMigrationTestsConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.configuration;

import com.google.common.collect.ImmutableMap;
import org.nrg.framework.configuration.FrameworkConfig;
import org.nrg.framework.test.OrmTestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Map;

@Configuration
@EnableTransactionManagement
@Import({DefaultResolverConfiguration.class, FrameworkConfig.class})
@ComponentScan("org.nrg.prefs.tools.alias")
public class AliasMigrationTestsConfiguration extends OrmTestConfiguration {
    private static final Map<String, String> EXTRA_HIBERNATE_PROPERTIES = ImmutableMap.of("hibernate.hbm2ddl.import_files", "/org/nrg/prefs/tests/init-aliased-prefs.sql");

    protected Map<String, String> getExtraHibernateProperties() {
        return EXTRA_HIBERNATE_PROPERTIES;
    }
}
