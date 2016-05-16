/*
 * org.nrg.xnat.security.config.DatabaseAuthenticationProviderConfigurator
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 12/11/13 3:33 PM
 */
package org.nrg.xnat.security.config;

import org.nrg.xdat.preferences.InitializerSiteConfiguration;
import org.nrg.xnat.security.provider.XnatDatabaseAuthenticationProvider;
import org.nrg.xnat.security.userdetailsservices.XnatDatabaseUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.ReflectionSaltSource;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseAuthenticationProviderConfigurator extends AbstractAuthenticationProviderConfigurator {
    public DatabaseAuthenticationProviderConfigurator() {
        setConfiguratorId("db");
    }

    @Override
    public List<AuthenticationProvider> getAuthenticationProviders(String id, String name) {
        List<AuthenticationProvider> providers = new ArrayList<>();

        ReflectionSaltSource saltSource = new ReflectionSaltSource();
        saltSource.setUserPropertyToUse("salt");

        XnatDatabaseAuthenticationProvider sha2DatabaseAuthProvider = new XnatDatabaseAuthenticationProvider(_preferences.getEmailVerification());
        sha2DatabaseAuthProvider.setUserDetailsService(_detailsService);
        sha2DatabaseAuthProvider.setPasswordEncoder(new ShaPasswordEncoder(256));
        sha2DatabaseAuthProvider.setName(name);
        sha2DatabaseAuthProvider.setProviderId(id);
        sha2DatabaseAuthProvider.setSaltSource(saltSource);
        providers.add(sha2DatabaseAuthProvider);

        return providers;
    }

    @Override
    public List<AuthenticationProvider> getAuthenticationProviders(String id, String name, Map<String, String> properties) {
        return getAuthenticationProviders(id, name);
    }

    @Autowired
    @Lazy
    private XnatDatabaseUserDetailsService _detailsService;

    @Autowired
    private InitializerSiteConfiguration _preferences;
}
