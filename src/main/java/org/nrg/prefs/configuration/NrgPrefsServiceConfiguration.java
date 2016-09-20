/*
 * org.nrg.prefs.configuration.NrgPrefsServiceConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.configuration;

import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.impl.DefaultNrgPreferenceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"org.nrg.prefs.services.impl.hibernate", "org.nrg.prefs.repositories"})
public class NrgPrefsServiceConfiguration {
    @Bean
    public HibernateEntityPackageList nrgPrefsHibernateEntityPackageList() {
        return new HibernateEntityPackageList("org.nrg.prefs.entities");
    }

    @Bean
    public NrgPreferenceService nrgPreferenceService() {
        return new DefaultNrgPreferenceService();
    }
}
